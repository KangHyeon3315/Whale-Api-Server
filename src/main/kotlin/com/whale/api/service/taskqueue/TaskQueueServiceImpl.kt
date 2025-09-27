package com.whale.api.service.taskqueue

import com.fasterxml.jackson.databind.ObjectMapper
import com.whale.api.model.taskqueue.TaskQueueEntity
import com.whale.api.repository.taskqueue.TaskQueueRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID

@Service
class TaskQueueServiceImpl(
    private val taskQueueRepository: TaskQueueRepository,
    private val objectMapper: ObjectMapper,
    private val writeTransactionTemplate: TransactionTemplate,
) : TaskQueueService {
    
    private val logger = KotlinLogging.logger {}
    
    override fun createTask(type: String, payload: Any): TaskQueueEntity {
        return writeTransactionTemplate.execute {
            val payloadJson = objectMapper.writeValueAsString(payload)
            
            val task = TaskQueueEntity(
                identifier = UUID.randomUUID(),
                type = type,
                payload = payloadJson,
                status = "PENDING",
                createdAt = LocalDateTime.now(),
                processedAt = null,
                errorMessage = null,
                retryCount = 0
            )
            
            taskQueueRepository.save(task)
            logger.info("Created task: ${task.identifier}, type: $type")
            task
        } ?: throw RuntimeException("Failed to create task")
    }
    
    override fun markAsProcessing(taskId: UUID): TaskQueueEntity? {
        return writeTransactionTemplate.execute {
            val task = taskQueueRepository.findById(taskId).orElse(null)
            if (task != null && task.status == "PENDING") {
                val updatedTask = task.copy(
                    status = "PROCESSING",
                    processedAt = LocalDateTime.now()
                )
                taskQueueRepository.save(updatedTask)
                logger.info("Marked task as processing: $taskId")
                updatedTask
            } else {
                logger.warn("Task not found or not in PENDING status: $taskId")
                null
            }
        }
    }
    
    override fun markAsCompleted(taskId: UUID): TaskQueueEntity? {
        return writeTransactionTemplate.execute {
            val task = taskQueueRepository.findById(taskId).orElse(null)
            if (task != null && task.status == "PROCESSING") {
                val updatedTask = task.copy(
                    status = "COMPLETED",
                    processedAt = LocalDateTime.now(),
                    errorMessage = null
                )
                taskQueueRepository.save(updatedTask)
                logger.info("Marked task as completed: $taskId")
                updatedTask
            } else {
                logger.warn("Task not found or not in PROCESSING status: $taskId")
                null
            }
        }
    }
    
    override fun markAsFailed(taskId: UUID, errorMessage: String): TaskQueueEntity? {
        return writeTransactionTemplate.execute {
            val task = taskQueueRepository.findById(taskId).orElse(null)
            if (task != null) {
                val updatedTask = task.copy(
                    status = "FAILED",
                    processedAt = LocalDateTime.now(),
                    errorMessage = errorMessage,
                    retryCount = task.retryCount + 1
                )
                taskQueueRepository.save(updatedTask)
                logger.error("Marked task as failed: $taskId, error: $errorMessage")
                updatedTask
            } else {
                logger.warn("Task not found: $taskId")
                null
            }
        }
    }
    
    override fun getPendingTasks(limit: Int): List<TaskQueueEntity> {
        return taskQueueRepository.findPendingTasksWithRetryLimit(3)
            .take(limit)
    }
    
    override fun markAsRetry(taskId: UUID): TaskQueueEntity? {
        return writeTransactionTemplate.execute {
            val task = taskQueueRepository.findById(taskId).orElse(null)
            if (task != null && task.status == "FAILED") {
                val updatedTask = task.copy(
                    status = "PENDING",
                    errorMessage = null
                )
                taskQueueRepository.save(updatedTask)
                logger.info("Marked task for retry: $taskId")
                updatedTask
            } else {
                logger.warn("Task not found or not in FAILED status: $taskId")
                null
            }
        }
    }

    override fun getFailedTasksForRetry(maxRetryCount: Int): List<TaskQueueEntity> {
        val retryAfter = LocalDateTime.now().minusMinutes(5) // 5분 후 재시도
        return taskQueueRepository.findFailedTasksForRetry(maxRetryCount, retryAfter)
    }
}
