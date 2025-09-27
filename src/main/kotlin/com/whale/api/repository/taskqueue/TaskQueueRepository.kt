package com.whale.api.repository.taskqueue

import com.whale.api.model.taskqueue.TaskQueueEntity
import com.whale.api.model.taskqueue.enums.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface TaskQueueRepository : JpaRepository<TaskQueueEntity, UUID> {
    
    @Query("""
        SELECT t FROM TaskQueueEntity t
        WHERE t.status = :status
        ORDER BY t.createdAt ASC
    """)
    fun findByStatusOrderByCreatedAtAsc(@Param("status") status: TaskStatus): List<TaskQueueEntity>

    @Query("""
        SELECT t FROM TaskQueueEntity t
        WHERE t.status = com.whale.api.model.taskqueue.enums.TaskStatus.PENDING
        AND t.retryCount < :maxRetryCount
        ORDER BY t.createdAt ASC
    """)
    fun findPendingTasksWithRetryLimit(@Param("maxRetryCount") maxRetryCount: Int): List<TaskQueueEntity>

    @Query("""
        SELECT t FROM TaskQueueEntity t
        WHERE t.status = com.whale.api.model.taskqueue.enums.TaskStatus.FAILED
        AND t.retryCount < :maxRetryCount
        AND t.createdAt < :retryAfter
        ORDER BY t.createdAt ASC
    """)
    fun findFailedTasksForRetry(
        @Param("maxRetryCount") maxRetryCount: Int,
        @Param("retryAfter") retryAfter: OffsetDateTime
    ): List<TaskQueueEntity>
    
    fun findByType(type: String): List<TaskQueueEntity>
}
