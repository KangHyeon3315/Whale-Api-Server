package com.whale.api.archive.application

import com.whale.api.archive.application.port.`in`.ArchiveItemPage
import com.whale.api.archive.application.port.`in`.GetArchiveItemContentUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemsUseCase
import com.whale.api.archive.application.port.`in`.UploadArchiveItemUseCase
import com.whale.api.archive.application.port.`in`.command.UploadArchiveItemCommand
import com.whale.api.archive.application.port.out.FileStorageOutput
import com.whale.api.archive.application.port.out.FindArchiveItemOutput
import com.whale.api.archive.application.port.out.FindArchiveMetadataOutput
import com.whale.api.archive.application.port.out.FindArchiveOutput
import com.whale.api.archive.application.port.out.MetadataExtractionOutput
import com.whale.api.archive.application.port.out.ReadArchiveItemContentOutput
import com.whale.api.archive.application.port.out.SaveArchiveItemOutput
import com.whale.api.archive.application.port.out.SaveArchiveMetadataOutput
import com.whale.api.archive.application.port.out.SaveArchiveOutput
import com.whale.api.archive.domain.ArchiveItem
import com.whale.api.archive.domain.ArchiveMetadata
import com.whale.api.archive.domain.property.ArchiveProperty
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ArchiveItemService(
    private val saveArchiveItemOutput: SaveArchiveItemOutput,
    private val findArchiveItemOutput: FindArchiveItemOutput,
    private val saveArchiveMetadataOutput: SaveArchiveMetadataOutput,
    private val findArchiveMetadataOutput: FindArchiveMetadataOutput,
    private val findArchiveOutput: FindArchiveOutput,
    private val saveArchiveOutput: SaveArchiveOutput,
    private val fileStorageOutput: FileStorageOutput,
    private val metadataExtractionOutput: MetadataExtractionOutput,
    private val archiveProperty: ArchiveProperty,
    private val readArchiveItemContentOutput: ReadArchiveItemContentOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : UploadArchiveItemUseCase,
    GetArchiveItemsUseCase,
    GetArchiveItemContentUseCase {
    private val logger = KotlinLogging.logger {}

    override fun uploadItem(command: UploadArchiveItemCommand): ArchiveItem {
        logger.info { "Uploading item to archive: ${command.archiveIdentifier}" }

        return writeTransactionTemplate.execute {
            // 1. Archive 존재 확인
            val archive =
                findArchiveOutput.findArchiveById(command.archiveIdentifier)
                    ?: throw IllegalArgumentException("Archive not found: ${command.archiveIdentifier}")

            // 2. 파일 확장자 검증
            val fileName = command.file.originalFilename ?: throw IllegalArgumentException("File name is required")
            val extension = fileName.substringAfterLast('.', "")
            if (!archiveProperty.isAllowedExtension(".$extension")) {
                throw IllegalArgumentException("File extension not allowed: $extension")
            }

            // 3. 파일 크기 검증
            if (command.file.size > archiveProperty.maxFileSize) {
                throw IllegalArgumentException("File size exceeds maximum allowed size")
            }

            // 4. 파일 저장 (파일 타입에 따라 경로 분류)
            val fileCategory = archiveProperty.getFileCategory(".$extension")
            val relativePath = "${archive.identifier}/$fileCategory/${UUID.randomUUID()}"
            val storedPath = fileStorageOutput.storeFile(command.file, relativePath)

            // 5. 라이브 포토 비디오 저장 (있는 경우)
            var livePhotoVideoPath: String? = null
            if (command.isLivePhoto && command.livePhotoVideo != null) {
                val videoRelativePath = "${archive.identifier}/live-photos/${UUID.randomUUID()}"
                livePhotoVideoPath = fileStorageOutput.storeFile(command.livePhotoVideo, videoRelativePath)
            }

            // 6. 체크섬 계산
            val checksum = fileStorageOutput.calculateChecksum(command.file)

            // 7. ArchiveItem 생성
            val archiveItem =
                ArchiveItem(
                    identifier = UUID.randomUUID(),
                    archiveIdentifier = command.archiveIdentifier,
                    originalPath = command.originalPath,
                    storedPath = storedPath,
                    fileName = fileName,
                    fileSize = command.file.size,
                    mimeType = archiveProperty.getMimeType(".$extension"),
                    isLivePhoto = command.isLivePhoto,
                    livePhotoVideoPath = livePhotoVideoPath,
                    checksum = checksum,
                    originalCreatedDate = command.originalCreatedDate,
                    originalModifiedDate = command.originalModifiedDate,
                    createdDate = OffsetDateTime.now(),
                    modifiedDate = OffsetDateTime.now(),
                )

            val savedItem = saveArchiveItemOutput.save(archiveItem)

            // 8. 메타데이터 추출 및 저장
            try {
                val extractedMetadata =
                    if (command.isLivePhoto && command.livePhotoVideo != null) {
                        metadataExtractionOutput.extractLivePhotoMetadata(
                            command.file,
                            command.livePhotoVideo,
                            savedItem.identifier,
                        )
                    } else {
                        metadataExtractionOutput.extractMetadata(command.file, savedItem.identifier)
                    }

                // 추가 메타데이터가 있으면 함께 저장
                val allMetadata = extractedMetadata.toMutableList()
                command.metadata.forEach { (key, value) ->
                    allMetadata.add(
                        ArchiveMetadata.createCustomMetadata(savedItem.identifier, key, value),
                    )
                }

                if (allMetadata.isNotEmpty()) {
                    saveArchiveMetadataOutput.saveAll(allMetadata)
                }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to extract metadata for item: ${savedItem.identifier}" }
            }

            // 9. Archive 진행 상태 업데이트 및 자동 완료 체크
            val isCompleted = archive.incrementProcessedItemsAndCheckCompletion()
            saveArchiveOutput.save(archive)

            if (isCompleted) {
                logger.info {
                    "Archive automatically completed: ${archive.identifier} " +
                        "(${archive.processedItems}/${archive.totalItems} items processed)"
                }
            }

            savedItem
        } ?: throw RuntimeException("Failed to upload archive item")
    }

    override fun getArchiveItems(archiveIdentifier: UUID): List<ArchiveItem> {
        return findArchiveItemOutput.findByArchiveIdentifier(archiveIdentifier)
    }

    override fun getArchiveItems(
        archiveIdentifier: UUID,
        fileName: String?,
        tags: List<String>?,
    ): List<ArchiveItem> {
        logger.info { "Searching archive items: archiveId=$archiveIdentifier, fileName=$fileName, tags=$tags" }
        return findArchiveItemOutput.findByArchiveIdentifierWithFilters(archiveIdentifier, fileName, tags)
    }

    override fun getArchiveItems(
        archiveIdentifier: UUID,
        fileName: String?,
        tags: List<String>?,
        cursor: OffsetDateTime?,
        limit: Int,
    ): ArchiveItemPage {
        logger.info {
            "Searching archive items with pagination: archiveId=$archiveIdentifier, fileName=$fileName, " +
                "tags=$tags, cursor=$cursor, limit=$limit"
        }

        // limit + 1로 조회해서 hasNext 판단
        val items =
            findArchiveItemOutput.findByArchiveIdentifierWithFiltersAndPagination(
                archiveIdentifier,
                fileName,
                tags,
                cursor,
                limit + 1,
            )

        val hasNext = items.size > limit
        val resultItems = if (hasNext) items.dropLast(1) else items

        val totalCount = findArchiveItemOutput.countByArchiveIdentifierWithFilters(archiveIdentifier, fileName, tags)

        logger.info { "Retrieved ${resultItems.size} items (hasNext=$hasNext, totalCount=$totalCount)" }

        return ArchiveItemPage(
            items = resultItems,
            hasNext = hasNext,
            totalCount = totalCount,
        )
    }

    override fun getArchiveItem(itemIdentifier: UUID): ArchiveItem {
        return findArchiveItemOutput.findArchiveItemById(itemIdentifier)
            ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")
    }

    override fun getArchiveItemMetadata(itemIdentifier: UUID): List<ArchiveMetadata> {
        return findArchiveMetadataOutput.findByArchiveItemIdentifier(itemIdentifier)
    }

    override fun getTextContent(itemIdentifier: UUID): String {
        val item =
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

        if (!item.isText()) {
            throw IllegalArgumentException("Item is not a text file: $itemIdentifier")
        }

        if (!readArchiveItemContentOutput.fileExists(item.storedPath)) {
            throw IllegalArgumentException("File not found: ${item.storedPath}")
        }

        return readArchiveItemContentOutput.readTextContent(item.storedPath)
    }

    override fun getTextContentPreview(
        itemIdentifier: UUID,
        maxLength: Int,
    ): String {
        val item =
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

        if (!item.isText()) {
            throw IllegalArgumentException("Item is not a text file: $itemIdentifier")
        }

        if (!readArchiveItemContentOutput.fileExists(item.storedPath)) {
            throw IllegalArgumentException("File not found: ${item.storedPath}")
        }

        return readArchiveItemContentOutput.readTextContentPreview(item.storedPath, maxLength)
    }
}
