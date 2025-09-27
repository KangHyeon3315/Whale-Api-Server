package com.whale.api.model.file

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
    @Column(name = "modified_date", nullable = true)
    var modifiedDate: OffsetDateTime?,
    @Column(name = "last_view_date", nullable = true)
    var lastViewDate: OffsetDateTime?,
)
