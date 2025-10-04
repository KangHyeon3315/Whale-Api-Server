package com.whale.api.file.adapter.input.scheduler

import com.whale.api.file.application.port.`in`.SaveFileUseCase
import com.whale.api.global.annotation.Scheduler
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled

@Scheduler
class SaveFileWorker(
    private val saveFileUseCase: SaveFileUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    fun processPendingSaveTasks() {
        try {
            logger.debug("Starting to process pending save tasks")
            saveFileUseCase.save()
            logger.debug("Completed processing pending save tasks")
        } catch (e: Exception) {
            logger.error("Error occurred while processing pending save tasks", e)
        }
    }
}
