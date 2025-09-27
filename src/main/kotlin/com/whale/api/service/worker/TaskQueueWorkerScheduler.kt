package com.whale.api.service.worker

import com.whale.api.service.taskqueue.TaskQueueService
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TaskQueueWorkerScheduler(
    private val taskQueueService: TaskQueueService,
    private val fileSaveWorker: FileSaveWorker,
) {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    @Async
    fun processPendingTasks() {
        try {
            val pendingTasks = taskQueueService.getPendingTasks(10)

            if (pendingTasks.isNotEmpty()) {
                logger.info("Processing ${pendingTasks.size} pending tasks")

                pendingTasks.forEach { task ->
                    try {
                        taskQueueService.markAsProcessing(task.identifier)

                        when (task.type) {
                            "FILE_SAVE" -> {
                                fileSaveWorker.processFileSaveTask(task)
                            }
                            else -> {
                                logger.warn("Unknown task type: ${task.type}")
                                taskQueueService.markAsFailed(task.identifier, "Unknown task type: ${task.type}")
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to process task: ${task.identifier}", e)
                        taskQueueService.markAsFailed(task.identifier, e.message ?: "Unknown error")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error in task queue worker scheduler", e)
        }
    }

//    @Scheduled(fixedDelay = 60000) // 1분마다 실행
//    @Async
    fun retryFailedTasks() {
        try {
            val failedTasks = taskQueueService.getFailedTasksForRetry(3)

            if (failedTasks.isNotEmpty()) {
                logger.info("Retrying ${failedTasks.size} failed tasks")

                failedTasks.forEach { task ->
                    try {
                        taskQueueService.markAsRetry(task.identifier)
                        logger.info("Marked task for retry: ${task.identifier}")
                    } catch (e: Exception) {
                        logger.error("Failed to retry task: ${task.identifier}", e)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error in retry failed tasks scheduler", e)
        }
    }
}
