package com.whale.api.model.taskqueue

import com.whale.api.model.taskqueue.enums.TaskStatus
import jakarta.persistence.*
import java.time.OffsetDateTime
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: TaskStatus,
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime,
    @Column(name = "processed_at", nullable = true)
    var processedAt: OffsetDateTime?,
    @Column(name = "error_message", nullable = true, columnDefinition = "text")
    var errorMessage: String?,
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int,
)
