package com.whale.api.archive.adapter.output.persistence.entity

import com.whale.api.archive.domain.ArchiveItemTag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "archive_item_tag")
data class ArchiveItemTagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "archive_item_identifier", nullable = false)
    val archiveItemIdentifier: UUID,
    @ManyToOne
    @JoinColumn(name = "tag_identifier", nullable = false)
    val tag: ArchiveTagEntity,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
) {
    fun toDomain(): ArchiveItemTag {
        return ArchiveItemTag(
            identifier = this.identifier,
            archiveItemIdentifier = this.archiveItemIdentifier,
            tagIdentifier = this.tag.identifier,
            createdDate = this.createdDate,
        )
    }

    companion object {
        fun ArchiveItemTag.toEntity(tagEntity: ArchiveTagEntity): ArchiveItemTagEntity {
            return ArchiveItemTagEntity(
                identifier = this.identifier,
                archiveItemIdentifier = this.archiveItemIdentifier,
                tag = tagEntity,
                createdDate = this.createdDate,
            )
        }
    }
}
