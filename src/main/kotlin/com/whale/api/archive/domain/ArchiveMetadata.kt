package com.whale.api.archive.domain

import com.whale.api.archive.domain.enums.MetadataType
import java.time.OffsetDateTime
import java.util.UUID

class ArchiveMetadata(
    val identifier: UUID,
    val archiveItemIdentifier: UUID,
    val metadataType: MetadataType,
    val key: String,
    val value: String,
    val createdDate: OffsetDateTime,
) {
    fun isGpsData(): Boolean {
        return metadataType == MetadataType.GPS
    }

    fun isExifData(): Boolean {
        return metadataType == MetadataType.EXIF
    }

    fun isCameraData(): Boolean {
        return metadataType == MetadataType.CAMERA
    }

    fun isDeviceData(): Boolean {
        return metadataType == MetadataType.DEVICE
    }

    fun isLivePhotoData(): Boolean {
        return metadataType == MetadataType.LIVE_PHOTO
    }

    fun isCustomData(): Boolean {
        return metadataType == MetadataType.CUSTOM
    }

    fun isTextContentData(): Boolean {
        return metadataType == MetadataType.TEXT_CONTENT
    }

    fun isDocumentPropertiesData(): Boolean {
        return metadataType == MetadataType.DOCUMENT_PROPERTIES
    }

    fun isFileEncodingData(): Boolean {
        return metadataType == MetadataType.FILE_ENCODING
    }

    companion object {
        fun createGpsMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.GPS,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createExifMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.EXIF,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createCameraMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.CAMERA,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createDeviceMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.DEVICE,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createLivePhotoMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.LIVE_PHOTO,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createCustomMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.CUSTOM,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createTextContentMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.TEXT_CONTENT,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createDocumentPropertiesMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.DOCUMENT_PROPERTIES,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }

        fun createFileEncodingMetadata(
            archiveItemIdentifier: UUID,
            key: String,
            value: String,
        ): ArchiveMetadata {
            return ArchiveMetadata(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                metadataType = MetadataType.FILE_ENCODING,
                key = key,
                value = value,
                createdDate = OffsetDateTime.now(),
            )
        }
    }
}
