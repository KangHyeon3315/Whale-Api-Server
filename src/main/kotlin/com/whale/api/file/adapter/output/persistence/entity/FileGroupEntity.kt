package com.whale.api.file.adapter.output.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "file_group")
data class FileGroupEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "thumbnail", nullable = true)
    val thumbnail: String?,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = true)
    var modifiedDate: OffsetDateTime?,
)
