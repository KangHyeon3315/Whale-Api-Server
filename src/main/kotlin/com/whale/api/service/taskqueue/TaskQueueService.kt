package com.whale.api.service.taskqueue

import com.whale.api.model.taskqueue.TaskQueueEntity
import java.util.UUID

interface TaskQueueService {
    fun createTask(type: String, payload: Any): TaskQueueEntity
    fun markAsProcessing(taskId: UUID): TaskQueueEntity?
    fun markAsCompleted(taskId: UUID): TaskQueueEntity?
    fun markAsFailed(taskId: UUID, errorMessage: String): TaskQueueEntity?
    fun markAsRetry(taskId: UUID): TaskQueueEntity?
    fun getPendingTasks(limit: Int = 10): List<TaskQueueEntity>
    fun getFailedTasksForRetry(maxRetryCount: Int = 3): List<TaskQueueEntity>
}
