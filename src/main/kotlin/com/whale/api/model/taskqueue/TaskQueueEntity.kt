package com.whale.api.model.taskqueue

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "task_queue")
data class TaskQueueEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "type", nullable = false, length = 50)
    val type: String,
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    val payload: String,
    @Column(name = "status", nullable = false, length = 20)
    val status: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "processed_at", nullable = true)
    var processedAt: LocalDateTime?,
    @Column(name = "error_message", nullable = true, columnDefinition = "text")
    var errorMessage: String?,
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int,
)
