package com.whale.api.file.adapter.output.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "save_task")
data class SaveTaskEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "path", nullable = false)
    val path: String,
    @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "tag_requests", nullable = false, columnDefinition = "jsonb")
    val tagRequests: String,
    @Column(name = "status", nullable = false)
    val status: String,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    var modifiedDate: OffsetDateTime,
)
