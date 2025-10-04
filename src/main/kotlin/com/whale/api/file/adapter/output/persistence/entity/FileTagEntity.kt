package com.whale.api.file.adapter.output.persistence.entity

import com.whale.api.file.domain.FileTag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "file_tag")
data class FileTagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "file_identifier", nullable = false)
    val fileIdentifier: UUID,
    @Column(name = "tag_identifier", nullable = false)
    val tagIdentifier: UUID,
) {
    fun toDomain(): FileTag {
        return FileTag(
            identifier = this.identifier,
            fileIdentifier = this.fileIdentifier,
            tagIdentifier = this.tagIdentifier,
        )
    }

    companion object {
        fun FileTag.toEntity(): FileTagEntity {
            return FileTagEntity(
                identifier = this.identifier,
                fileIdentifier = this.fileIdentifier,
                tagIdentifier = this.tagIdentifier,
            )
        }
    }
}
