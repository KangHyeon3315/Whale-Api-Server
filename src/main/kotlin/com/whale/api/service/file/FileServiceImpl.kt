package com.whale.api.service.file

import com.whale.api.controller.file.request.*
import com.whale.api.controller.file.response.*
import com.whale.api.global.config.MediaFileProperty
import com.whale.api.model.file.exception.FileNotFoundException
import com.whale.api.model.file.exception.InvalidPathException
import com.whale.api.model.file.exception.UnsupportedMediaFileTypeException
import com.whale.api.repository.file.*
import com.whale.api.model.taskqueue.dto.FileSaveTaskPayload
import com.whale.api.model.taskqueue.dto.TagDto
import com.whale.api.service.taskqueue.TaskQueueService
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import javax.imageio.ImageIO

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
) : FileService {

    private val logger = KotlinLogging.logger {}

    override fun getUnsortedTree(path: String, cursor: String?, limit: Int, sort: String): FileTreeResponse {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val targetPath = basePath.resolve(path)

        validatePath(targetPath)

        if (!Files.isDirectory(targetPath)) {
            throw InvalidPathException("Directory not found: $targetPath")
        }

        val fileDetails = mutableListOf<FileTreeItem>()

        Files.list(targetPath).use { stream ->
            stream.forEach { filePath ->
                val fileName = filePath.fileName.toString()
                val isDir = Files.isDirectory(filePath)
                val extension = if (!isDir) {
                    val dotIndex = fileName.lastIndexOf('.')
                    if (dotIndex > 0) fileName.substring(dotIndex + 1).lowercase() else ""
                } else ""

                fileDetails.add(
                    FileTreeItem(
                        name = fileName,
                        isDir = isDir,
                        extension = extension
                    )
                )
            }
        }

        // 정렬 로직
        val sortedFiles = when (sort) {
            "number" -> {
                fileDetails.sortedWith(compareBy<FileTreeItem> { !it.isDir }
                    .thenBy { file ->
                        val numbers = Regex("(\\d+)").findAll(file.name)
                            .map { it.value.toInt() }
                            .toList()
                        numbers.firstOrNull() ?: 0
                    }
                    .thenBy { it.name })
            }
            else -> {
                fileDetails.sortedWith(compareBy<FileTreeItem> { !it.isDir }
                    .thenBy { it.name })
            }
        }

        // 커서 기반 페이지네이션
        val filteredFiles = cursor?.let { cursorName ->
            val cursorIndex = sortedFiles.indexOfFirst { it.name == cursorName }
            if (cursorIndex >= 0) sortedFiles.drop(cursorIndex + 1) else sortedFiles
        } ?: sortedFiles

        // 리밋 적용
        val limitedFiles = filteredFiles.take(limit)

        return FileTreeResponse(files = limitedFiles)
    }

    override fun getThumbnail(path: String): ResponseEntity<Resource> {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val filePath = basePath.resolve(path)

        validatePath(filePath)

        if (!Files.isRegularFile(filePath)) {
            throw FileNotFoundException("File not found: $path")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        val isImage = extension in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }
        val isVideo = extension in MediaFileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") }

        if (!isImage && !isVideo) {
            throw UnsupportedMediaFileTypeException("Unsupported media file type: $extension")
        }

        val thumbnailPath = generateThumbnailPath(path)

        // 이미 썸네일이 존재하면 반환
        if (Files.exists(thumbnailPath)) {
            val resource = org.springframework.core.io.FileSystemResource(thumbnailPath)
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource)
        }

        // 썸네일 생성
        return if (isImage) {
            generateImageThumbnail(filePath, thumbnailPath)
        } else {
            generateVideoThumbnail(filePath, thumbnailPath)
        }
    }

    private fun generateThumbnailPath(originalPath: String): Path {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val thumbnailDir = basePath.resolve(mediaFileProperty.thumbnailPath)
        Files.createDirectories(thumbnailDir)
        return thumbnailDir.resolve("$originalPath.thumbnail.jpg")
    }

    private fun generateImageThumbnail(filePath: Path, thumbnailPath: Path): ResponseEntity<Resource> {
        try {
            Files.createDirectories(thumbnailPath.parent)

            val originalImage = ImageIO.read(filePath.toFile())
            val thumbnailImage = createThumbnail(originalImage, 512, 512)

            ImageIO.write(thumbnailImage, "jpg", thumbnailPath.toFile())

            val resource = org.springframework.core.io.FileSystemResource(thumbnailPath)
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource)
        } catch (e: Exception) {
            logger.error("Error generating image thumbnail for $filePath", e)
            throw RuntimeException("Failed to generate thumbnail", e)
        }
    }

    private fun generateVideoThumbnail(filePath: Path, thumbnailPath: Path): ResponseEntity<Resource> {
        try {
            Files.createDirectories(thumbnailPath.parent)

            // FFmpeg를 사용한 비디오 썸네일 생성
            val processBuilder = ProcessBuilder(
                "ffmpeg",
                "-i", filePath.toString(),
                "-ss", "1",
                "-vframes", "1",
                "-vf", "scale=512:-1",
                "-y",
                thumbnailPath.toString()
            )

            val process = processBuilder.start()
            val exitCode = process.waitFor()

            if (exitCode == 0 && Files.exists(thumbnailPath) && Files.size(thumbnailPath) > 0) {
                val resource = org.springframework.core.io.FileSystemResource(thumbnailPath)
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(resource)
            } else {
                logger.error("FFmpeg failed to generate thumbnail for $filePath, exit code: $exitCode")
                throw RuntimeException("Failed to generate video thumbnail")
            }
        } catch (e: Exception) {
            logger.error("Error generating video thumbnail for $filePath", e)
            throw RuntimeException("Failed to generate thumbnail", e)
        }
    }

    private fun createThumbnail(originalImage: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        val ratio = minOf(maxWidth.toDouble() / originalWidth, maxHeight.toDouble() / originalHeight)
        val newWidth = (originalWidth * ratio).toInt()
        val newHeight = (originalHeight * ratio).toInt()

        val scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
        val thumbnailImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)

        val graphics = thumbnailImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        return thumbnailImage
    }

    override fun getImage(path: String): ResponseEntity<Resource> {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val filePath = basePath.resolve(path)

        validatePath(filePath)

        if (!Files.isRegularFile(filePath)) {
            throw FileNotFoundException("File not found: $path")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        if (extension !in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }) {
            throw UnsupportedMediaFileTypeException("Unsupported image file type: $extension")
        }

        val resource = org.springframework.core.io.FileSystemResource(filePath)
        val mimeType = MediaFileProperty.MIME_TYPE_MAPPING[".$extension"] ?: "image/*"

        return ResponseEntity.ok()
            .header("Content-Type", mimeType)
            .body(resource)
    }

    override fun getVideo(path: String, range: String?): ResponseEntity<Resource> {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val filePath = basePath.resolve(path)

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw FileNotFoundException("File not found: $path")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        if (extension !in MediaFileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") }) {
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
        val basePath = Paths.get(mediaFileProperty.basePath)
        val filePath = basePath.resolve(request.path)

        validatePath(filePath)

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

        val fileTags = fileTagRepository.findByFileIdentifiersWithTag(listOf(fileIdentifier))
        val tags = fileTags.map { fileTag ->
            TagResponse(
                identifier = fileTag.tag.identifier,
                name = fileTag.tag.name,
                type = fileTag.tag.type
            )
        }

        return FileResponse(
            identifier = file.identifier,
            fileGroupIdentifier = file.fileGroup.identifier,
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
            val basePath = Paths.get(mediaFileProperty.basePath)
            val sourcePath = basePath.resolve(request.path)

            validatePath(sourcePath)

            if (!Files.isDirectory(sourcePath)) {
                throw FileNotFoundException("Directory not found: ${request.path}")
            }

            val identifier = UUID.randomUUID()
            val newPath = Paths.get(mediaFileProperty.filesPath, "group", identifier.toString())
            val newFullPath = basePath.resolve(newPath)

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
                            path = newPath.resolve(newFileName).toString(),
                            thumbnail = null,
                            sortOrder = index,
                            createdDate = now,
                            modifiedDate = now,
                            lastViewDate = null
                        )

                        files.add(fileEntity)

                        // 파일 해시 계산 (이미지 파일만)
                        if (extension in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }) {
                            try {
                                val hash = calculateFileHash(newFilePath)
                                hashes.add(
                                    com.whale.api.model.file.FileHashEntity(
                                        identifier = UUID.randomUUID(),
                                        file = fileEntity,
                                        hash = hash
                                    )
                                )
                            } catch (e: Exception) {
                                logger.warn("Failed to calculate hash for file: $newFilePath", e)
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
                fileGroupIdentifier = file.fileGroup.identifier,
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
                fileGroupIdentifier = file.fileGroup.identifier,
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

    private fun calculateFileHash(filePath: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(filePath).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun validatePath(path: Path) {
        val pathStr = path.toString()
        if (pathStr.contains("..")) {
            throw InvalidPathException("Path contains invalid characters: $pathStr")
        }

        if (!Files.exists(path)) {
            throw InvalidPathException("Path does not exist: $pathStr")
        }
    }
}
