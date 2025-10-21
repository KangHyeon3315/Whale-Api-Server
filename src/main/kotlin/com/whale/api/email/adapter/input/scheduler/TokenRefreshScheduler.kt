package com.whale.api.email.adapter.input.scheduler

import com.whale.api.email.application.port.out.FindEmailAccountOutput
import com.whale.api.email.application.port.out.GmailProviderOutput
import com.whale.api.email.application.port.out.SaveEmailAccountOutput
import com.whale.api.email.domain.EmailProvider
import com.whale.api.global.annotation.Scheduler
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import java.time.OffsetDateTime

@Scheduler
class TokenRefreshScheduler(
    private val findEmailAccountOutput: FindEmailAccountOutput,
    private val saveEmailAccountOutput: SaveEmailAccountOutput,
    private val gmailProviderOutput: GmailProviderOutput,
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Gmail 토큰 갱신 스케줄러
     * 매 1시간마다 실행하여 만료 예정인 토큰들을 갱신
     */
    @Async
    @Scheduled(fixedDelay = 3600000) // 1시간 = 60 * 60 * 1000ms
    fun refreshExpiredTokens() {
        logger.debug { "Starting token refresh check" }
        
        try {
            // 1시간 이내에 만료될 Gmail 계정들 조회
            val expiringAccounts = findEmailAccountOutput.findAccountsWithExpiringTokens(
                provider = EmailProvider.GMAIL,
                expiryThresholdHours = 1
            )
            
            if (expiringAccounts.isEmpty()) {
                logger.debug { "No expiring tokens found" }
                return
            }
            
            logger.info { "Found ${expiringAccounts.size} Gmail accounts with expiring tokens" }
            
            expiringAccounts.forEach { account ->
                try {
                    if (account.refreshToken == null) {
                        logger.warn { "No refresh token available for account: ${account.emailAddress}" }
                        return@forEach
                    }
                    
                    logger.debug { "Refreshing token for account: ${account.emailAddress}" }
                    
                    val tokenInfo = gmailProviderOutput.refreshAccessToken(account.refreshToken)
                    
                    val updatedAccount = account.copy(
                        accessToken = tokenInfo.accessToken,
                        refreshToken = tokenInfo.refreshToken ?: account.refreshToken,
                        tokenExpiry = OffsetDateTime.now().plusSeconds(tokenInfo.expiresInSeconds),
                        modifiedDate = OffsetDateTime.now()
                    )
                    
                    saveEmailAccountOutput.save(updatedAccount)
                    
                    logger.info { "Successfully refreshed token for account: ${account.emailAddress}" }
                    
                } catch (e: Exception) {
                    logger.error(e) { "Failed to refresh token for account: ${account.emailAddress}" }
                    
                    // 토큰 갱신 실패 시 계정을 비활성화할지 결정
                    if (isTokenRefreshFailureCritical(e)) {
                        logger.warn { "Disabling account due to critical token refresh failure: ${account.emailAddress}" }
                        
                        val disabledAccount = account.copy(
                            isActive = false,
                            modifiedDate = OffsetDateTime.now()
                        )
                        
                        saveEmailAccountOutput.save(disabledAccount)
                    }
                }
            }
            
            logger.info { "Completed token refresh check" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during token refresh" }
        }
    }
    
    /**
     * 토큰 상태 모니터링
     * 매 6시간마다 실행하여 토큰 상태를 점검
     */
    @Async
    @Scheduled(fixedDelay = 21600000) // 6시간 = 6 * 60 * 60 * 1000ms
    fun monitorTokenHealth() {
        logger.debug { "Starting token health monitoring" }
        
        try {
            val gmailAccounts = findEmailAccountOutput.findAllActiveByProvider(EmailProvider.GMAIL)
            
            if (gmailAccounts.isEmpty()) {
                logger.debug { "No Gmail accounts found for monitoring" }
                return
            }
            
            logger.info { "Monitoring token health for ${gmailAccounts.size} Gmail accounts" }
            
            var healthyTokens = 0
            var expiringTokens = 0
            var expiredTokens = 0
            var missingRefreshTokens = 0
            
            gmailAccounts.forEach { account ->
                when {
                    account.refreshToken == null -> {
                        missingRefreshTokens++
                        logger.warn { "Missing refresh token for account: ${account.emailAddress}" }
                    }
                    account.tokenExpiry == null -> {
                        logger.warn { "Missing token expiry for account: ${account.emailAddress}" }
                    }
                    account.tokenExpiry!!.isBefore(OffsetDateTime.now()) -> {
                        expiredTokens++
                        logger.warn { "Expired token for account: ${account.emailAddress}" }
                    }
                    account.tokenExpiry!!.isBefore(OffsetDateTime.now().plusHours(24)) -> {
                        expiringTokens++
                        logger.info { "Token expiring within 24 hours for account: ${account.emailAddress}" }
                    }
                    else -> {
                        healthyTokens++
                    }
                }
            }
            
            logger.info { 
                "Token health summary - Healthy: $healthyTokens, " +
                "Expiring (24h): $expiringTokens, Expired: $expiredTokens, " +
                "Missing refresh token: $missingRefreshTokens"
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during token health monitoring" }
        }
    }
    
    /**
     * 주간 토큰 정리 작업
     * 매주 일요일 새벽 3시에 실행
     */
    @Async
    @Scheduled(cron = "0 0 3 * * SUN") // 매주 일요일 새벽 3시
    fun weeklyTokenCleanup() {
        logger.info { "Starting weekly token cleanup" }
        
        try {
            // 30일 이상 만료된 계정들 조회
            val longExpiredAccounts = findEmailAccountOutput.findAccountsWithLongExpiredTokens(
                provider = EmailProvider.GMAIL,
                expiredDays = 30
            )
            
            if (longExpiredAccounts.isEmpty()) {
                logger.info { "No long-expired accounts found for cleanup" }
                return
            }
            
            logger.info { "Found ${longExpiredAccounts.size} long-expired accounts for cleanup" }
            
            longExpiredAccounts.forEach { account ->
                try {
                    logger.info { "Disabling long-expired account: ${account.emailAddress}" }
                    
                    val disabledAccount = account.copy(
                        isActive = false,
                        syncEnabled = false,
                        modifiedDate = OffsetDateTime.now()
                    )
                    
                    saveEmailAccountOutput.save(disabledAccount)
                    
                } catch (e: Exception) {
                    logger.error(e) { "Failed to disable long-expired account: ${account.emailAddress}" }
                }
            }
            
            logger.info { "Completed weekly token cleanup" }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during weekly token cleanup" }
        }
    }
    
    private fun isTokenRefreshFailureCritical(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("invalid_grant") || 
               message.contains("unauthorized") ||
               message.contains("revoked")
    }
}
