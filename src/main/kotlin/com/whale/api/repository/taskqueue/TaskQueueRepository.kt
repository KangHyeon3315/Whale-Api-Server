package com.whale.api.repository.taskqueue

import com.whale.api.model.taskqueue.TaskQueueEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface TaskQueueRepository : JpaRepository<TaskQueueEntity, UUID> {
    
    @Query("""
        SELECT t FROM TaskQueueEntity t 
        WHERE t.status = :status 
        ORDER BY t.createdAt ASC
    """)
    fun findByStatusOrderByCreatedAtAsc(@Param("status") status: String): List<TaskQueueEntity>
    
    @Query("""
        SELECT t FROM TaskQueueEntity t 
        WHERE t.status = 'PENDING' 
        AND t.retryCount < :maxRetryCount
        ORDER BY t.createdAt ASC
    """)
    fun findPendingTasksWithRetryLimit(@Param("maxRetryCount") maxRetryCount: Int): List<TaskQueueEntity>
    
    @Query("""
        SELECT t FROM TaskQueueEntity t 
        WHERE t.status = 'FAILED' 
        AND t.retryCount < :maxRetryCount
        AND t.createdAt < :retryAfter
        ORDER BY t.createdAt ASC
    """)
    fun findFailedTasksForRetry(
        @Param("maxRetryCount") maxRetryCount: Int,
        @Param("retryAfter") retryAfter: LocalDateTime
    ): List<TaskQueueEntity>
    
    fun findByType(type: String): List<TaskQueueEntity>
}
