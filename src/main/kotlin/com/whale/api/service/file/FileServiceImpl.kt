package com.whale.api.service.file

import com.whale.api.controller.file.request.*
import com.whale.api.controller.file.response.*
import com.whale.api.file.adapter.output.persistence.repository.FileGroupRepository
import com.whale.api.file.adapter.output.persistence.repository.FileGroupTagRepository
import com.whale.api.file.adapter.output.persistence.repository.FileHashRepository
import com.whale.api.file.adapter.output.persistence.repository.FileRepository
import com.whale.api.file.adapter.output.persistence.repository.FileTagRepository
import com.whale.api.file.adapter.output.persistence.repository.TagRepository
import com.whale.api.file.adapter.output.persistence.repository.UnsortedFileRepository
import com.whale.api.global.config.MediaFileProperty
import com.whale.api.file.domain.exception.FileNotFoundException
import com.whale.api.file.domain.exception.UnsupportedMediaFileTypeException
import com.whale.api.repository.file.*
import com.whale.api.model.taskqueue.dto.FileSaveTaskPayload
import com.whale.api.model.taskqueue.dto.TagDto
import com.whale.api.service.file.util.*
import com.whale.api.service.taskqueue.TaskQueueService
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class FileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileGroupRepository: FileGroupRepository,
    private val tagRepository: TagRepository,
    private val unsortedFileRepository: UnsortedFileRepository,
    private val fileTagRepository: FileTagRepository,
    private val fileGroupTagRepository: FileGroupTagRepository,
    private val fileHashRepository: FileHashRepository,
    private val mediaFileProperty: MediaFileProperty,
    private val taskQueueService: TaskQueueService,
    private val writeTransactionTemplate: TransactionTemplate,
    // Utility classes
    private val filePathUtil: FilePathUtil,
    private val fileHashUtil: FileHashUtil,
    private val thumbnailUtil: ThumbnailUtil,
    private val fileSortUtil: FileSortUtil,
    private val fileTreeBuilder: FileTreeBuilder,
    private val tagUtil: TagUtil,
) : FileService {

    private val logger = KotlinLogging.logger {}

    override fun getUnsortedTree(path: String, cursor: String?, limit: Int, sort: String): FileTreeResponse {
        val targetPath = filePathUtil.resolveBasePath(path)
        filePathUtil.validatePath(targetPath)
        filePathUtil.validateDirectoryExists(targetPath, "Directory not found: $path")

        // 파일 트리 구성
        val fileDetails = fileTreeBuilder.buildFileTree(targetPath)

        // 정렬 적용
        val sortedFiles = fileSortUtil.sortFiles(fileDetails, sort)

        // 커서 기반 필터링
        val filteredFiles = fileSortUtil.filterFilesByCursor(sortedFiles, cursor)

        // 리밋 적용
        val limitedFiles = filteredFiles.take(limit)

        return FileTreeResponse(files = limitedFiles)
    }

    override fun getThumbnail(path: String): ResponseEntity<Resource> {
        val filePath = filePathUtil.resolveBasePath(path)
        filePathUtil.validatePath(filePath)
        filePathUtil.validateFileExists(filePath, "File not found: $path")

        val extension = filePathUtil.getFileExtension(filePath)
        val isImage = filePathUtil.isImageFile(extension)
        val isVideo = filePathUtil.isVideoFile(extension)

        if (!isImage && !isVideo) {
            throw UnsupportedMediaFileTypeException("Unsupported media file type: $extension")
        }

        val thumbnailPath = filePathUtil.createThumbnailPath(path)

        // 이미 썸네일이 존재하면 반환
        if (thumbnailUtil.thumbnailExists(thumbnailPath)) {
            return thumbnailUtil.getThumbnailResource(thumbnailPath)
        }

        // 썸네일 생성
        return if (isImage) {
            thumbnailUtil.generateImageThumbnail(filePath, thumbnailPath)
        } else {
            thumbnailUtil.generateVideoThumbnail(filePath, thumbnailPath)
        }
    }



    override fun getImage(path: String): ResponseEntity<Resource> {
        val filePath = filePathUtil.resolveBasePath(path)
        filePathUtil.validatePath(filePath)
        filePathUtil.validateFileExists(filePath, "File not found: $path")

        val extension = filePathUtil.getFileExtension(filePath)
        if (!filePathUtil.isImageFile(extension)) {
            throw UnsupportedMediaFileTypeException("Unsupported image file type: $extension")
        }

        val resource = org.springframework.core.io.FileSystemResource(filePath)
        val mimeType = MediaFileProperty.MIME_TYPE_MAPPING[".$extension"] ?: "image/*"

        return ResponseEntity.ok()
            .header("Content-Type", mimeType)
            .body(resource)
    }

    override fun getVideo(path: String, range: String?): ResponseEntity<Resource> {
        val filePath = filePathUtil.resolveBasePath(path)
        filePathUtil.validateFileExists(filePath, "File not found: $path")

        val extension = filePathUtil.getFileExtension(filePath)
        if (!filePathUtil.isVideoFile(extension)) {
            throw UnsupportedMediaFileTypeException("Unsupported video file type: $extension")
        }

        val fileSize = Files.size(filePath)
        val mimeType = MediaFileProperty.MIME_TYPE_MAPPING[".$extension"] ?: "video/*"

        return if (range != null) {
            handleRangeRequest(filePath, range, fileSize, mimeType)
        } else {
            val resource = org.springframework.core.io.FileSystemResource(filePath)
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .header(HttpHeaders.CONTENT_LENGTH, fileSize.toString())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource)
        }
    }

    private fun handleRangeRequest(filePath: Path, range: String, fileSize: Long, mimeType: String): ResponseEntity<Resource> {
        val rangeMatch = Regex("bytes=(\\d+)-(\\d*)").find(range)
        if (rangeMatch == null) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build()
        }

        val start = rangeMatch.groupValues[1].toLong()
        val end = if (rangeMatch.groupValues[2].isNotEmpty()) {
            rangeMatch.groupValues[2].toLong()
        } else {
            fileSize - 1
        }

        if (start >= fileSize || end >= fileSize || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build()
        }

        val contentLength = end - start + 1
        val resource = RangeResource(filePath, start, contentLength)

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .header(HttpHeaders.CONTENT_TYPE, mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, contentLength.toString())
            .header(HttpHeaders.CONTENT_RANGE, "bytes $start-$end/$fileSize")
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .body(resource)
    }

    override fun deleteFileByPath(request: DeleteFileByPathRequest) {
        val filePath = filePathUtil.resolveBasePath(request.path)
        filePathUtil.validatePath(filePath)

        if (!Files.exists(filePath)) {
            throw FileNotFoundException("File not found: ${request.path}")
        }

        Files.delete(filePath)
        logger.info("Deleted file: $filePath")
    }

    override fun findAllTypes(): List<String> {
        return fileRepository.findDistinctTypes()
    }

    override fun findAllTags(): List<TagResponse> {
        return tagRepository.findAll().map { tag ->
            TagResponse(
                identifier = tag.identifier,
                name = tag.name,
                type = tag.type
            )
        }
    }

    override fun saveFile(request: SaveFileRequest): SaveFileResponse {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val filePath = basePath.resolve(request.path)

        // 파일 경로 유효성 검증
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw FileNotFoundException("File not found: ${request.path}")
        }

        val fileIdentifier = UUID.randomUUID()

        // TaskQueue에 파일 저장 작업 추가
        val payload = FileSaveTaskPayload(
            fileIdentifier = fileIdentifier,
            name = request.name,
            path = request.path,
            type = request.type,
            tags = request.tags.map { TagDto(name = it.name, type = it.type) },
            basePath = mediaFileProperty.basePath
        )

        val task = taskQueueService.createTask("FILE_SAVE", payload)

        logger.info("File save task created: ${task.identifier} for file: ${request.path}")

        return SaveFileResponse(
            message = "File save request submitted successfully",
            eventId = task.identifier,
            fileIdentifier = fileIdentifier,
            status = "PENDING"
        )
    }

    override fun findFile(fileIdentifier: UUID): FileResponse {
        val file = fileRepository.findById(fileIdentifier)
            .orElseThrow { FileNotFoundException("File not found: $fileIdentifier") }

        val tagMap = tagUtil.getFileTagResponses(listOf(fileIdentifier))
        val tags = tagMap[fileIdentifier] ?: emptyList()

        return FileResponse(
            identifier = file.identifier,
            fileGroupIdentifier = file.fileGroup?.identifier,
            name = file.name,
            type = file.type,
            path = file.path,
            thumbnail = file.thumbnail,
            sortOrder = file.sortOrder,
            createdDate = file.createdDate,
            modifiedDate = file.modifiedDate,
            lastViewDate = file.lastViewDate,
            tags = tags
        )
    }

    override fun updateFile(fileIdentifier: UUID, request: UpdateFileRequest): FileResponse {
        return writeTransactionTemplate.execute {
            val file = fileRepository.findById(fileIdentifier)
                .orElseThrow { FileNotFoundException("File not found: $fileIdentifier") }

            // 파일 정보 업데이트
            val updatedFile = file.copy(
                name = request.name ?: file.name,
                thumbnail = request.thumbnail ?: file.thumbnail,
                sortOrder = request.sortOrder ?: file.sortOrder,
                modifiedDate = java.time.OffsetDateTime.now()
            )

            fileRepository.save(updatedFile)

            // 태그 업데이트
            request.tags?.let { tagIdentifiers ->
                fileTagRepository.deleteByFileIdentifier(fileIdentifier)

                val newFileTags = tagIdentifiers.map { tagIdentifier ->
                    val tag = tagRepository.findById(tagIdentifier)
                        .orElseThrow { FileNotFoundException("Tag not found: $tagIdentifier") }

                    com.whale.api.model.file.FileTagEntity(
                        identifier = UUID.randomUUID(),
                        file = updatedFile,
                        tag = tag
                    )
                }

                fileTagRepository.saveAll(newFileTags)
            }

            findFile(fileIdentifier)
        } ?: throw RuntimeException("Transaction failed")
    }

    override fun deleteFile(fileIdentifier: UUID) {
        writeTransactionTemplate.execute {
            val file = fileRepository.findById(fileIdentifier)
                .orElseThrow { FileNotFoundException("File not found: $fileIdentifier") }

            // 관련 데이터 삭제
            fileTagRepository.deleteByFileIdentifier(fileIdentifier)
            fileHashRepository.deleteByFileIdentifier(fileIdentifier)
            fileRepository.delete(file)

            // 실제 파일 삭제
            try {
                val basePath = Paths.get(mediaFileProperty.basePath)
                val filePath = basePath.resolve(file.path)
                if (Files.exists(filePath)) {
                    Files.delete(filePath)
                    logger.info("Deleted file: $filePath")
                }
            } catch (e: Exception) {
                logger.error("Failed to delete physical file: ${file.path}", e)
            }
        }
    }

    override fun saveFileGroup(request: SaveFileGroupRequest): FileGroupResponse {
        return writeTransactionTemplate.execute {
            val sourcePath = filePathUtil.resolveBasePath(request.path)
            filePathUtil.validatePath(sourcePath)
            filePathUtil.validateDirectoryExists(sourcePath, "Directory not found: ${request.path}")

            val identifier = UUID.randomUUID()
            val newFullPath = filePathUtil.createFileGroupPath(identifier.toString())

            val now = java.time.OffsetDateTime.now()
            val fileGroup = com.whale.api.model.file.FileGroupEntity(
                identifier = identifier,
                name = request.name,
                type = request.type,
                thumbnail = request.thumbnail,
                createdDate = now,
                modifiedDate = now
            )

            fileGroupRepository.save(fileGroup)

            // 파일들을 새 위치로 이동하고 FileEntity 생성
            Files.createDirectories(newFullPath)
            val files = mutableListOf<com.whale.api.model.file.FileEntity>()
            val hashes = mutableListOf<com.whale.api.model.file.FileHashEntity>()

            Files.list(sourcePath).use { stream ->
                val sortedFiles = stream.sorted().toList()
                sortedFiles.forEachIndexed { index: Int, sourceFile: Path ->
                    if (Files.isRegularFile(sourceFile)) {
                        val fileIdentifier = UUID.randomUUID()
                        val originalFileName = sourceFile.fileName.toString()
                        val nameWithoutExtension = originalFileName.substringBeforeLast('.')
                        val extension = originalFileName.substringAfterLast('.').lowercase()

                        val newFileName = "$fileIdentifier.$extension"
                        val newFilePath = newFullPath.resolve(newFileName)

                        // 파일 이동
                        Files.move(sourceFile, newFilePath)

                        val fileEntity = com.whale.api.model.file.FileEntity(
                            identifier = fileIdentifier,
                            fileGroup = fileGroup,
                            name = nameWithoutExtension,
                            type = request.type,
                            path = "group/${identifier}/$newFileName",
                            thumbnail = null,
                            sortOrder = index,
                            createdDate = now,
                            modifiedDate = now,
                            lastViewDate = null
                        )

                        files.add(fileEntity)

                        // 파일 해시 계산 (이미지 파일만)
                        if (filePathUtil.isImageFile(extension)) {
                            fileHashUtil.calculateFileHashSafely(newFilePath)?.let { hash ->
                                hashes.add(
                                    com.whale.api.model.file.FileHashEntity(
                                        identifier = UUID.randomUUID(),
                                        file = fileEntity,
                                        hash = hash
                                    )
                                )
                            }
                        }
                    }
                }
            }

            fileRepository.saveAll(files)
            fileHashRepository.saveAll(hashes)

            // 파일 그룹 태그 저장
            val fileGroupTags = request.tags.map { tagIdentifier ->
                val tag = tagRepository.findById(tagIdentifier)
                    .orElseThrow { FileNotFoundException("Tag not found: $tagIdentifier") }

                com.whale.api.model.file.FileGroupTagEntity(
                    identifier = UUID.randomUUID(),
                    fileGroup = fileGroup,
                    tag = tag
                )
            }

            fileGroupTagRepository.saveAll(fileGroupTags)

            findFileGroup(identifier)
        } ?: throw RuntimeException("Transaction failed")
    }

    override fun findFileGroup(fileGroupIdentifier: UUID): FileGroupResponse {
        val fileGroup = fileGroupRepository.findById(fileGroupIdentifier)
            .orElseThrow { FileNotFoundException("File group not found: $fileGroupIdentifier") }

        val files = fileRepository.findByFileGroupIdentifierOrderBySortOrder(fileGroupIdentifier)
        val fileResponses = files.map { file ->
            val fileTags = fileTagRepository.findByFileIdentifiersWithTag(listOf(file.identifier))
            val tags = fileTags.map { fileTag ->
                TagResponse(
                    identifier = fileTag.tag.identifier,
                    name = fileTag.tag.name,
                    type = fileTag.tag.type
                )
            }

            FileResponse(
                identifier = file.identifier,
                fileGroupIdentifier = file.fileGroup?.identifier,
                name = file.name,
                type = file.type,
                path = file.path,
                thumbnail = file.thumbnail,
                sortOrder = file.sortOrder,
                createdDate = file.createdDate,
                modifiedDate = file.modifiedDate,
                lastViewDate = file.lastViewDate,
                tags = tags
            )
        }

        val fileGroupTags = fileGroupTagRepository.findByFileGroupIdentifiersWithTag(listOf(fileGroupIdentifier))
        val tags = fileGroupTags.map { fileGroupTag ->
            TagResponse(
                identifier = fileGroupTag.tag.identifier,
                name = fileGroupTag.tag.name,
                type = fileGroupTag.tag.type
            )
        }

        return FileGroupResponse(
            identifier = fileGroup.identifier,
            name = fileGroup.name,
            type = fileGroup.type,
            thumbnail = fileGroup.thumbnail,
            createdDate = fileGroup.createdDate,
            modifiedDate = fileGroup.modifiedDate,
            files = fileResponses,
            tags = tags
        )
    }

    override fun updateFileGroup(fileGroupIdentifier: UUID, request: UpdateFileGroupRequest): FileGroupResponse {
        return writeTransactionTemplate.execute {
            val fileGroup = fileGroupRepository.findById(fileGroupIdentifier)
                .orElseThrow { FileNotFoundException("File group not found: $fileGroupIdentifier") }

            val updatedFileGroup = fileGroup.copy(
                name = request.name ?: fileGroup.name,
                thumbnail = request.thumbnail ?: fileGroup.thumbnail,
                modifiedDate = java.time.OffsetDateTime.now()
            )

            fileGroupRepository.save(updatedFileGroup)

            // 태그 업데이트
            request.tags?.let { tagIdentifiers ->
                fileGroupTagRepository.deleteByFileGroupIdentifier(fileGroupIdentifier)

                val newFileGroupTags = tagIdentifiers.map { tagIdentifier ->
                    val tag = tagRepository.findById(tagIdentifier)
                        .orElseThrow { FileNotFoundException("Tag not found: $tagIdentifier") }

                    com.whale.api.model.file.FileGroupTagEntity(
                        identifier = UUID.randomUUID(),
                        fileGroup = updatedFileGroup,
                        tag = tag
                    )
                }

                fileGroupTagRepository.saveAll(newFileGroupTags)
            }

            findFileGroup(fileGroupIdentifier)
        } ?: throw RuntimeException("Transaction failed")
    }

    override fun deleteFileGroup(fileGroupIdentifier: UUID) {
        writeTransactionTemplate.execute {
            val fileGroup = fileGroupRepository.findById(fileGroupIdentifier)
                .orElseThrow { FileNotFoundException("File group not found: $fileGroupIdentifier") }

            val files = fileRepository.findByFileGroupIdentifier(fileGroupIdentifier)

            // 관련 데이터 삭제
            files.forEach { file ->
                fileTagRepository.deleteByFileIdentifier(file.identifier)
                fileHashRepository.deleteByFileIdentifier(file.identifier)
            }

            fileGroupTagRepository.deleteByFileGroupIdentifier(fileGroupIdentifier)
            fileRepository.deleteAll(files)
            fileGroupRepository.delete(fileGroup)

            // 실제 디렉토리 삭제
            try {
                val basePath = Paths.get(mediaFileProperty.basePath)
                val groupPath = basePath.resolve(mediaFileProperty.filesPath)
                    .resolve("group")
                    .resolve(fileGroupIdentifier.toString())

                if (Files.exists(groupPath)) {
                    Files.walk(groupPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach { Files.delete(it) }
                    logger.info("Deleted directory: $groupPath")
                }
            } catch (e: Exception) {
                logger.error("Failed to delete physical directory for file group: $fileGroupIdentifier", e)
            }
        }
    }

    override fun searchFileGroups(
        type: String?,
        tags: String?,
        keyword: String?,
        sort: String,
        order: String,
        cursor: String?,
        limit: Int
    ): List<FileGroupResponse> {
        // 간단한 구현 - 복잡한 쿼리는 추후 QueryDSL로 개선
        val fileGroups = fileGroupRepository.searchFileGroups(type, keyword, sort, order)

        return fileGroups.take(limit).map { fileGroup ->
            val fileGroupTags = fileGroupTagRepository.findByFileGroupIdentifiersWithTag(listOf(fileGroup.identifier))
            val tagResponses = fileGroupTags.map { fileGroupTag ->
                TagResponse(
                    identifier = fileGroupTag.tag.identifier,
                    name = fileGroupTag.tag.name,
                    type = fileGroupTag.tag.type
                )
            }

            FileGroupResponse(
                identifier = fileGroup.identifier,
                name = fileGroup.name,
                type = fileGroup.type,
                thumbnail = fileGroup.thumbnail,
                createdDate = fileGroup.createdDate,
                modifiedDate = fileGroup.modifiedDate,
                files = emptyList(), // 검색 결과에서는 파일 목록 제외
                tags = tagResponses
            )
        }
    }

    override fun findFileGroupFiles(
        fileGroupIdentifier: UUID,
        type: String?,
        tags: String?,
        keyword: String?,
        sort: String,
        order: String,
        cursor: String?,
        limit: Int
    ): List<FileResponse> {
        val files = fileRepository.findByFileGroupIdentifierOrderBySortOrder(fileGroupIdentifier)

        // 간단한 필터링 (추후 개선 필요)
        val filteredFiles = files.filter { file ->
            (type == null || file.type == type) &&
            (keyword == null || file.name.contains(keyword, ignoreCase = true))
        }.take(limit)

        val fileIdentifiers = filteredFiles.map { it.identifier }
        val fileTags = fileTagRepository.findByFileIdentifiersWithTag(fileIdentifiers)
        val fileTagsMap = fileTags.groupBy { it.file.identifier }

        return filteredFiles.map { file ->
            val tags = fileTagsMap[file.identifier]?.map { fileTag ->
                TagResponse(
                    identifier = fileTag.tag.identifier,
                    name = fileTag.tag.name,
                    type = fileTag.tag.type
                )
            } ?: emptyList()

            FileResponse(
                identifier = file.identifier,
                fileGroupIdentifier = file.fileGroup?.identifier,
                name = file.name,
                type = file.type,
                path = file.path,
                thumbnail = file.thumbnail,
                sortOrder = file.sortOrder,
                createdDate = file.createdDate,
                modifiedDate = file.modifiedDate,
                lastViewDate = file.lastViewDate,
                tags = tags
            )
        }
    }


}
