package com.whale.api.email.application.service

import com.whale.api.email.application.port.`in`.SyncEmailUseCase
import com.whale.api.email.application.port.out.FindEmailAccountOutput
import com.whale.api.email.application.port.out.FindEmailOutput
import com.whale.api.email.application.port.out.GmailProviderOutput
import com.whale.api.email.application.port.out.NaverMailProviderOutput
import com.whale.api.email.application.port.out.SaveEmailAccountOutput
import com.whale.api.email.application.port.out.SaveEmailOutput
import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import com.whale.api.email.domain.property.EmailProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class EmailSyncService(
    private val findEmailAccountOutput: FindEmailAccountOutput,
    private val saveEmailAccountOutput: SaveEmailAccountOutput,
    private val findEmailOutput: FindEmailOutput,
    private val saveEmailOutput: SaveEmailOutput,
    private val gmailProviderOutput: GmailProviderOutput,
    private val naverMailProviderOutput: NaverMailProviderOutput,
    private val emailProperty: EmailProperty,
) : SyncEmailUseCase {
    
    private val logger = LoggerFactory.getLogger(EmailSyncService::class.java)
    
    override fun syncAllAccounts(userId: String) {
        val accounts = findEmailAccountOutput.findAllActiveByUserId(userId)
        
        accounts.forEach { account ->
            try {
                syncSingleAccount(account)
            } catch (e: Exception) {
                logger.error("Failed to sync account ${account.emailAddress}", e)
            }
        }
    }
    
    override fun syncAccount(
        userId: String,
        accountId: UUID,
    ) {
        val account = findEmailAccountOutput.findByUserIdAndIdentifier(userId, accountId)
            ?: throw EmailAccountNotFoundException("Email account not found: $accountId")
        
        if (!account.isActive || !account.syncEnabled) {
            throw EmailSyncException("Email account is not active or sync is disabled")
        }
        
        syncSingleAccount(account)
    }
    
    override fun syncAccountFolder(
        userId: String,
        accountId: UUID,
        folderName: String,
    ) {
        val account = findEmailAccountOutput.findByUserIdAndIdentifier(userId, accountId)
            ?: throw EmailAccountNotFoundException("Email account not found: $accountId")
        
        if (!account.isActive || !account.syncEnabled) {
            throw EmailSyncException("Email account is not active or sync is disabled")
        }
        
        when (account.provider) {
            EmailProvider.GMAIL -> syncGmailFolder(account, folderName)
            EmailProvider.NAVER -> syncNaverFolder(account, folderName)
        }
        
        // 마지막 동기화 시간 업데이트
        updateLastSyncDate(account)
    }
    
    private fun syncSingleAccount(account: EmailAccount) {
        logger.info("Starting sync for account: ${account.emailAddress}")
        
        try {
            when (account.provider) {
                EmailProvider.GMAIL -> syncGmailAccount(account)
                EmailProvider.NAVER -> syncNaverAccount(account)
            }
            
            // 마지막 동기화 시간 업데이트
            updateLastSyncDate(account)
            
            logger.info("Completed sync for account: ${account.emailAddress}")
        } catch (e: Exception) {
            logger.error("Failed to sync account: ${account.emailAddress}", e)
            throw EmailSyncException("Failed to sync account: ${account.emailAddress}", e)
        }
    }
    
    private fun syncGmailAccount(account: EmailAccount) {
        // 토큰 갱신 확인
        val updatedAccount = if (account.needsTokenRefresh()) {
            refreshGmailToken(account)
        } else {
            account
        }
        
        // 기본 폴더들 동기화
        val folders = listOf("INBOX", "SENT", "DRAFT")
        
        folders.forEach { folderName ->
            try {
                syncGmailFolder(updatedAccount, folderName)
            } catch (e: Exception) {
                logger.error("Failed to sync Gmail folder $folderName for ${account.emailAddress}", e)
            }
        }
    }
    
    private fun syncNaverAccount(account: EmailAccount) {
        // 기본 폴더들 동기화
        val folders = listOf("INBOX", "SENT", "DRAFT")
        
        folders.forEach { folderName ->
            try {
                syncNaverFolder(account, folderName)
            } catch (e: Exception) {
                logger.error("Failed to sync Naver folder $folderName for ${account.emailAddress}", e)
            }
        }
    }
    
    private fun syncGmailFolder(account: EmailAccount, folderName: String) {
        var pageToken: String? = null
        var syncedCount = 0
        
        do {
            val syncResult = gmailProviderOutput.getEmails(
                emailAccount = account,
                folderName = folderName,
                maxResults = emailProperty.maxEmailsPerSync,
                pageToken = pageToken
            )
            
            // 새로운 이메일만 필터링
            val newEmails = syncResult.emails.filter { email ->
                !findEmailOutput.existsByAccountIdentifierAndMessageId(
                    account.identifier,
                    email.messageId
                )
            }
            
            if (newEmails.isNotEmpty()) {
                saveEmailOutput.saveAll(newEmails)
                syncedCount += newEmails.size
                logger.debug("Synced ${newEmails.size} new emails from Gmail folder $folderName")
            }
            
            pageToken = syncResult.nextPageToken
            
            // 최대 동기화 수 제한
            if (syncedCount >= emailProperty.maxEmailsPerSync) {
                break
            }
            
        } while (syncResult.hasMore && pageToken != null)
        
        logger.info("Gmail folder $folderName sync completed: $syncedCount new emails")
    }
    
    private fun syncNaverFolder(account: EmailAccount, folderName: String) {
        val emails = naverMailProviderOutput.getEmails(
            emailAccount = account,
            folderName = folderName,
            maxResults = emailProperty.maxEmailsPerSync
        )
        
        // 새로운 이메일만 필터링
        val newEmails = emails.filter { email ->
            !findEmailOutput.existsByAccountIdentifierAndMessageId(
                account.identifier,
                email.messageId
            )
        }
        
        if (newEmails.isNotEmpty()) {
            saveEmailOutput.saveAll(newEmails)
            logger.info("Naver folder $folderName sync completed: ${newEmails.size} new emails")
        } else {
            logger.debug("No new emails found in Naver folder $folderName")
        }
    }
    
    private fun refreshGmailToken(account: EmailAccount): EmailAccount {
        if (account.refreshToken == null) {
            throw EmailSyncException("No refresh token available for account: ${account.emailAddress}")
        }
        
        val tokenInfo = gmailProviderOutput.refreshAccessToken(account.refreshToken)
        
        val updatedAccount = account.copy(
            accessToken = tokenInfo.accessToken,
            refreshToken = tokenInfo.refreshToken ?: account.refreshToken,
            tokenExpiry = OffsetDateTime.now().plusSeconds(tokenInfo.expiresInSeconds),
            modifiedDate = OffsetDateTime.now()
        )
        
        return saveEmailAccountOutput.save(updatedAccount)
    }
    
    private fun updateLastSyncDate(account: EmailAccount) {
        val updatedAccount = account.copy(
            lastSyncDate = OffsetDateTime.now(),
            modifiedDate = OffsetDateTime.now()
        )
        
        saveEmailAccountOutput.save(updatedAccount)
    }
}

class EmailSyncException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
