package com.whale.api.email.adapter.input.scheduler

import com.whale.api.email.application.service.EmailAttachmentService
import com.whale.api.global.annotation.Scheduler
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

@Scheduler
class AttachmentCleanupScheduler(
    private val emailAttachmentService: EmailAttachmentService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 오래된 첨부파일 정리
     * 매일 새벽 4시에 실행
     */
    @Async
    @Scheduled(cron = "0 0 4 * * *") // 매일 새벽 4시
    fun cleanupOldAttachments() {
        logger.info { "Starting scheduled cleanup of old attachments" }

        try {
            emailAttachmentService.cleanupOldAttachments(daysOld = 30)
            logger.info { "Completed scheduled cleanup of old attachments" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during attachment cleanup" }
        }
    }

    /**
     * 주간 첨부파일 정리 (더 오래된 파일들)
     * 매주 일요일 새벽 5시에 실행
     */
    @Async
    @Scheduled(cron = "0 0 5 * * SUN") // 매주 일요일 새벽 5시
    fun weeklyAttachmentCleanup() {
        logger.info { "Starting weekly cleanup of very old attachments" }

        try {
            emailAttachmentService.cleanupOldAttachments(daysOld = 90)
            logger.info { "Completed weekly cleanup of very old attachments" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during weekly attachment cleanup" }
        }
    }
}
