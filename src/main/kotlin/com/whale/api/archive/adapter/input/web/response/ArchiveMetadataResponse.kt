package com.whale.api.archive.adapter.input.web.response

import com.whale.api.archive.domain.ArchiveMetadata
import com.whale.api.archive.domain.enums.MetadataType
import java.time.OffsetDateTime
import java.util.UUID

data class ArchiveMetadataResponse(
    val identifier: UUID,
    val archiveItemIdentifier: UUID,
    val metadataType: MetadataType,
    val key: String,
    val value: String,
    val createdDate: OffsetDateTime,
) {
    companion object {
        fun from(archiveMetadata: ArchiveMetadata): ArchiveMetadataResponse {
            return ArchiveMetadataResponse(
                identifier = archiveMetadata.identifier,
                archiveItemIdentifier = archiveMetadata.archiveItemIdentifier,
                metadataType = archiveMetadata.metadataType,
                key = archiveMetadata.key,
                value = archiveMetadata.value,
                createdDate = archiveMetadata.createdDate,
            )
        }
    }
}
