package com.whale.api.file.adapter.output.persistence.entity

import com.whale.api.file.domain.File
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "file")
data class FileEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @ManyToOne
    @JoinColumn(name = "file_group_identifier", nullable = true)
    val fileGroup: FileGroupEntity?,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "path", nullable = false)
    val path: String,
    @Column(name = "thumbnail", nullable = true)
    val thumbnail: String?,
    @Column(name = "sort_order", nullable = true)
    val sortOrder: Int?,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    var modifiedDate: OffsetDateTime,
    @Column(name = "last_view_date", nullable = true)
    var lastViewDate: OffsetDateTime?,
) {
    fun toDomain(): File {
        return File(
            identifier = this.identifier,
            fileGroupIdentifier = this.fileGroup?.identifier,
            name = this.name,
            type = this.type,
            path = this.path,
            thumbnail = this.thumbnail,
            sortOrder = this.sortOrder,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
            lastViewDate = this.lastViewDate,
        )
    }

    companion object {
        fun File.toEntity(): FileEntity {
            return FileEntity(
                identifier = this.identifier,
                fileGroup = null,
                name = this.name,
                type = this.type,
                path = this.path,
                thumbnail = this.thumbnail,
                sortOrder = this.sortOrder,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
                lastViewDate = this.lastViewDate,
            )
        }
    }
}
