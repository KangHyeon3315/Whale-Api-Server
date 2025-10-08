package com.whale.api.archive.application

import com.whale.api.archive.application.port.`in`.GetArchiveFileUseCase
import com.whale.api.archive.application.port.out.FindArchiveItemOutput
import com.whale.api.archive.application.port.out.ReadArchiveFileOutput
import com.whale.api.archive.domain.ArchiveFileResource
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArchiveFileQueryService(
    private val findArchiveItemOutput: FindArchiveItemOutput,
    private val readArchiveFileOutput: ReadArchiveFileOutput,
) : GetArchiveFileUseCase {
    private val logger = KotlinLogging.logger {}

    override fun getArchiveFile(
        itemIdentifier: UUID,
        rangeHeader: String?,
    ): ArchiveFileResource {
        logger.debug { "Getting archive file: $itemIdentifier, rangeHeader: $rangeHeader" }

        val archiveItem =
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

        // 파일 경로 검증
        if (!readArchiveFileOutput.validateFilePath(archiveItem.storedPath)) {
            throw RuntimeException("Archive file not found or invalid: ${archiveItem.storedPath}")
        }

        return if (rangeHeader != null) {
            readArchiveFileOutput.readArchiveFileWithRange(
                archiveItem.storedPath,
                archiveItem.fileName,
                archiveItem.mimeType,
                rangeHeader,
            )
        } else {
            readArchiveFileOutput.readArchiveFile(
                archiveItem.storedPath,
                archiveItem.fileName,
                archiveItem.mimeType,
            )
        }
    }

    override fun getArchiveFileThumbnail(itemIdentifier: UUID): ArchiveFileResource {
        logger.debug { "Getting archive file thumbnail: $itemIdentifier" }

        val archiveItem =
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

        // 이미지나 비디오 파일만 썸네일 생성 가능
        if (!readArchiveFileOutput.isImageFile(archiveItem.mimeType) &&
            !readArchiveFileOutput.isVideoFile(archiveItem.mimeType)
        ) {
            throw IllegalArgumentException("Thumbnail not supported for file type: ${archiveItem.mimeType}")
        }

        // 썸네일 생성 (이미 존재하면 기존 것 사용)
        val thumbnailPath = readArchiveFileOutput.createThumbnail(archiveItem.storedPath, archiveItem.mimeType)

        return readArchiveFileOutput.readThumbnail(thumbnailPath, "${archiveItem.fileName}_thumb.jpg")
    }

    override fun getLivePhotoVideo(
        itemIdentifier: UUID,
        rangeHeader: String?,
    ): ArchiveFileResource {
        logger.debug { "Getting live photo video: $itemIdentifier, rangeHeader: $rangeHeader" }

        val archiveItem =
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

        if (!archiveItem.isLivePhoto || archiveItem.livePhotoVideoPath == null) {
            throw IllegalArgumentException("Item is not a live photo or video path not found: $itemIdentifier")
        }

        // 비디오 파일 경로 검증
        if (!readArchiveFileOutput.validateFilePath(archiveItem.livePhotoVideoPath)) {
            throw RuntimeException("Live photo video file not found: ${archiveItem.livePhotoVideoPath}")
        }

        val videoFileName = "${archiveItem.fileName.substringBeforeLast('.')}.mov"

        return if (rangeHeader != null) {
            readArchiveFileOutput.readArchiveFileWithRange(
                archiveItem.livePhotoVideoPath,
                videoFileName,
                "video/quicktime",
                rangeHeader,
            )
        } else {
            readArchiveFileOutput.readArchiveFile(
                archiveItem.livePhotoVideoPath,
                videoFileName,
                "video/quicktime",
            )
        }
    }
}
