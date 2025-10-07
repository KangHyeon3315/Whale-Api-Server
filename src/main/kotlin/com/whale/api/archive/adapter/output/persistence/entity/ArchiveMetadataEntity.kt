package com.whale.api.archive.adapter.output.persistence.entity

import com.whale.api.archive.domain.ArchiveMetadata
import com.whale.api.archive.domain.enums.MetadataType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "archive_metadata")
data class ArchiveMetadataEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "archive_item_identifier", nullable = false)
    val archiveItemIdentifier: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_type", nullable = false)
    val metadataType: MetadataType,
    @Column(name = "key", nullable = false)
    val key: String,
    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    val value: String,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
) {
    fun toDomain(): ArchiveMetadata {
        return ArchiveMetadata(
            identifier = this.identifier,
            archiveItemIdentifier = this.archiveItemIdentifier,
            metadataType = this.metadataType,
            key = this.key,
            value = this.value,
            createdDate = this.createdDate,
        )
    }

    companion object {
        fun ArchiveMetadata.toEntity(): ArchiveMetadataEntity {
            return ArchiveMetadataEntity(
                identifier = this.identifier,
                archiveItemIdentifier = this.archiveItemIdentifier,
                metadataType = this.metadataType,
                key = this.key,
                value = this.value,
                createdDate = this.createdDate,
            )
        }
    }
}
