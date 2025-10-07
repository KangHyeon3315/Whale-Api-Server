package com.whale.api.archive.adapter.output.persistence.entity

import com.whale.api.archive.domain.ArchiveTag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "archive_tag")
data class ArchiveTagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
) {
    fun toDomain(): ArchiveTag {
        return ArchiveTag(
            identifier = this.identifier,
            name = this.name,
            type = this.type,
            createdDate = this.createdDate,
        )
    }

    companion object {
        fun ArchiveTag.toEntity(): ArchiveTagEntity {
            return ArchiveTagEntity(
                identifier = this.identifier,
                name = this.name,
                type = this.type,
                createdDate = this.createdDate,
            )
        }
    }
}
