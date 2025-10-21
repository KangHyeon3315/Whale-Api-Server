package com.whale.api.email.adapter.input.scheduler

import com.whale.api.email.application.port.`in`.SyncEmailUseCase
import com.whale.api.email.application.port.out.FindEmailAccountOutput
import com.whale.api.global.annotation.Scheduler
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import java.util.UUID

@Scheduler
class EmailSyncScheduler(
    private val syncEmailUseCase: SyncEmailUseCase,
    private val findEmailAccountOutput: FindEmailAccountOutput,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 모든 활성 계정의 자동 동기화
     * 매 30분마다 실행
     */
    @Async
    @Scheduled(fixedDelay = 1800000) // 30분 = 30 * 60 * 1000ms
    fun syncAllActiveAccounts() {
        logger.info { "Starting scheduled sync for all active email accounts" }

        try {
            val activeAccounts = findEmailAccountOutput.findAllActive()
            val userAccountMap = activeAccounts.groupBy { it.userId }

            logger.info { "Found ${activeAccounts.size} active accounts for ${userAccountMap.size} users" }

            userAccountMap.forEach { (userId, accounts) ->
                try {
                    logger.debug { "Syncing ${accounts.size} accounts for user: $userId" }
                    syncEmailUseCase.syncAllAccounts(userId.toString())
                    logger.debug { "Completed sync for user: $userId" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to sync accounts for user: $userId" }
                }
            }

            logger.info { "Completed scheduled sync for all active email accounts" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during scheduled email sync" }
        }
    }

    /**
     * 최근 동기화되지 않은 계정들의 우선 동기화
     * 매 10분마다 실행
     */
    @Async
    @Scheduled(fixedDelay = 600000) // 10분 = 10 * 60 * 1000ms
    fun syncStaleAccounts() {
        logger.debug { "Starting sync for stale email accounts" }

        try {
            val staleAccounts = findEmailAccountOutput.findStaleAccounts(hours = 2)

            if (staleAccounts.isEmpty()) {
                logger.debug { "No stale accounts found" }
                return
            }

            logger.info { "Found ${staleAccounts.size} stale accounts to sync" }

            val userAccountMap = staleAccounts.groupBy { it.userId }

            userAccountMap.forEach { (userId, accounts) ->
                accounts.forEach { account ->
                    try {
                        logger.debug { "Syncing stale account: ${account.emailAddress}" }
                        syncEmailUseCase.syncAccount(userId.toString(), account.identifier)
                        logger.debug { "Completed sync for stale account: ${account.emailAddress}" }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to sync stale account: ${account.emailAddress}" }
                    }
                }
            }

            logger.info { "Completed sync for stale email accounts" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during stale account sync" }
        }
    }

    /**
     * 특정 시간대에 집중 동기화 (새벽 2시)
     * 매일 새벽 2시에 실행
     */
    @Async
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    fun performDeepSync() {
        logger.info { "Starting deep sync for all email accounts" }

        try {
            val allAccounts = findEmailAccountOutput.findAllActive()
            val userAccountMap = allAccounts.groupBy { it.userId }

            logger.info { "Performing deep sync for ${allAccounts.size} accounts" }

            userAccountMap.forEach { (userId, accounts) ->
                accounts.forEach { account ->
                    try {
                        // 각 폴더별로 개별 동기화 수행
                        val folders = when (account.provider) {
                            com.whale.api.email.domain.EmailProvider.GMAIL ->
                                listOf("INBOX", "SENT", "DRAFT", "SPAM", "TRASH")
                            com.whale.api.email.domain.EmailProvider.NAVER ->
                                listOf("INBOX", "SENT", "DRAFT", "SPAM", "TRASH")
                        }

                        folders.forEach { folderName ->
                            try {
                                logger.debug { "Deep syncing folder $folderName for account: ${account.emailAddress}" }
                                syncEmailUseCase.syncAccountFolder(
                                    userId.toString(),
                                    account.identifier,
                                    folderName
                                )
                            } catch (e: Exception) {
                                logger.error(e) {
                                    "Failed to deep sync folder $folderName for account: ${account.emailAddress}"
                                }
                            }
                        }

                        logger.debug { "Completed deep sync for account: ${account.emailAddress}" }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to deep sync account: ${account.emailAddress}" }
                    }
                }
            }

            logger.info { "Completed deep sync for all email accounts" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during deep sync" }
        }
    }
}
