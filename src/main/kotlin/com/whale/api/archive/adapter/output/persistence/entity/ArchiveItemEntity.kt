package com.whale.api.archive.adapter.output.persistence.entity

import com.whale.api.archive.domain.ArchiveItem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "archive_item")
data class ArchiveItemEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "archive_identifier", nullable = false)
    val archiveIdentifier: UUID,
    @Column(name = "original_path", nullable = false)
    val originalPath: String,
    @Column(name = "stored_path", nullable = false)
    val storedPath: String,
    @Column(name = "file_name", nullable = false)
    val fileName: String,
    @Column(name = "file_size", nullable = false)
    val fileSize: Long,
    @Column(name = "mime_type", nullable = false)
    val mimeType: String,
    @Column(name = "is_live_photo", nullable = false)
    val isLivePhoto: Boolean,
    @Column(name = "live_photo_video_path", nullable = true)
    val livePhotoVideoPath: String?,
    @Column(name = "checksum", nullable = true)
    val checksum: String?,
    @Column(name = "original_created_date", nullable = true)
    val originalCreatedDate: OffsetDateTime?,
    @Column(name = "original_modified_date", nullable = true)
    val originalModifiedDate: OffsetDateTime?,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    val modifiedDate: OffsetDateTime,
) {
    fun toDomain(): ArchiveItem {
        return ArchiveItem(
            identifier = this.identifier,
            archiveIdentifier = this.archiveIdentifier,
            originalPath = this.originalPath,
            storedPath = this.storedPath,
            fileName = this.fileName,
            fileSize = this.fileSize,
            mimeType = this.mimeType,
            isLivePhoto = this.isLivePhoto,
            livePhotoVideoPath = this.livePhotoVideoPath,
            checksum = this.checksum,
            originalCreatedDate = this.originalCreatedDate,
            originalModifiedDate = this.originalModifiedDate,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
        )
    }

    companion object {
        fun ArchiveItem.toEntity(): ArchiveItemEntity {
            return ArchiveItemEntity(
                identifier = this.identifier,
                archiveIdentifier = this.archiveIdentifier,
                originalPath = this.originalPath,
                storedPath = this.storedPath,
                fileName = this.fileName,
                fileSize = this.fileSize,
                mimeType = this.mimeType,
                isLivePhoto = this.isLivePhoto,
                livePhotoVideoPath = this.livePhotoVideoPath,
                checksum = this.checksum,
                originalCreatedDate = this.originalCreatedDate,
                originalModifiedDate = this.originalModifiedDate,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
            )
        }
    }
}
