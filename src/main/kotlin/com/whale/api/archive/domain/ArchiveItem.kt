package com.whale.api.archive.domain

import java.time.OffsetDateTime
import java.util.UUID

class ArchiveItem(
    val identifier: UUID,
    val archiveIdentifier: UUID,
    val originalPath: String,
    val storedPath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val isLivePhoto: Boolean,
    val livePhotoVideoPath: String?,
    val checksum: String?,
    val originalCreatedDate: OffsetDateTime?,
    val originalModifiedDate: OffsetDateTime?,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime,
) {
    fun updateStoredPath(newPath: String): ArchiveItem {
        return ArchiveItem(
            identifier = this.identifier,
            archiveIdentifier = this.archiveIdentifier,
            originalPath = this.originalPath,
            storedPath = newPath,
            fileName = this.fileName,
            fileSize = this.fileSize,
            mimeType = this.mimeType,
            isLivePhoto = this.isLivePhoto,
            livePhotoVideoPath = this.livePhotoVideoPath,
            checksum = this.checksum,
            originalCreatedDate = this.originalCreatedDate,
            originalModifiedDate = this.originalModifiedDate,
            createdDate = this.createdDate,
            modifiedDate = OffsetDateTime.now(),
        )
    }

    fun updateLivePhotoVideoPath(videoPath: String): ArchiveItem {
        return ArchiveItem(
            identifier = this.identifier,
            archiveIdentifier = this.archiveIdentifier,
            originalPath = this.originalPath,
            storedPath = this.storedPath,
            fileName = this.fileName,
            fileSize = this.fileSize,
            mimeType = this.mimeType,
            isLivePhoto = this.isLivePhoto,
            livePhotoVideoPath = videoPath,
            checksum = this.checksum,
            originalCreatedDate = this.originalCreatedDate,
            originalModifiedDate = this.originalModifiedDate,
            createdDate = this.createdDate,
            modifiedDate = OffsetDateTime.now(),
        )
    }

    fun isImage(): Boolean {
        return mimeType.startsWith("image/")
    }

    fun isVideo(): Boolean {
        return mimeType.startsWith("video/")
    }

    fun isText(): Boolean {
        return mimeType.startsWith("text/") ||
            mimeType == "application/json" ||
            mimeType == "application/xml"
    }

    fun isDocument(): Boolean {
        return mimeType.startsWith("application/") &&
            (
                mimeType.contains("word") ||
                    mimeType.contains("excel") ||
                    mimeType.contains("powerpoint") ||
                    mimeType.contains("pdf")
            )
    }

    fun getFileExtension(): String {
        return fileName.substringAfterLast('.', "")
    }

    fun getFileCategory(): String {
        return when {
            isImage() -> "image"
            isVideo() -> "video"
            isText() -> "text"
            isDocument() -> "document"
            isLivePhoto -> "live_photo"
            else -> "other"
        }
    }
}
