package com.whale.api.archive.adapter.input.web.response

import com.whale.api.archive.domain.ArchiveItem
import java.time.OffsetDateTime
import java.util.UUID

data class ArchiveItemResponse(
    val identifier: UUID,
    val archiveIdentifier: UUID,
    val originalPath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileCategory: String,
    val isLivePhoto: Boolean,
    val hasLivePhotoVideo: Boolean,
    val checksum: String?,
    val originalCreatedDate: OffsetDateTime?,
    val originalModifiedDate: OffsetDateTime?,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    companion object {
        fun from(archiveItem: ArchiveItem): ArchiveItemResponse {
            return ArchiveItemResponse(
                identifier = archiveItem.identifier,
                archiveIdentifier = archiveItem.archiveIdentifier,
                originalPath = archiveItem.originalPath,
                fileName = archiveItem.fileName,
                fileSize = archiveItem.fileSize,
                mimeType = archiveItem.mimeType,
                fileCategory = archiveItem.getFileCategory(),
                isLivePhoto = archiveItem.isLivePhoto,
                hasLivePhotoVideo = archiveItem.livePhotoVideoPath != null,
                checksum = archiveItem.checksum,
                originalCreatedDate = archiveItem.originalCreatedDate,
                originalModifiedDate = archiveItem.originalModifiedDate,
                createdDate = archiveItem.createdDate,
                modifiedDate = archiveItem.modifiedDate,
            )
        }
    }
}
