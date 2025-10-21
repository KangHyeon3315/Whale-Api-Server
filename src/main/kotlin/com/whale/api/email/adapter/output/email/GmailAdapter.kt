package com.whale.api.email.adapter.output.email

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.ModifyMessageRequest
import com.whale.api.email.application.port.out.EmailSyncResult
import com.whale.api.email.application.port.out.GmailProviderOutput
import com.whale.api.email.application.port.out.TokenInfo
import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.property.EmailProperty
import org.springframework.stereotype.Component
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class GmailAdapter(
    private val emailProperty: EmailProperty,
) : GmailProviderOutput {
    
    private val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    
    override fun getAuthorizationUrl(userId: String): String {
        val flow = createAuthorizationFlow()
        return flow.newAuthorizationUrl()
            .setRedirectUri(emailProperty.gmailRedirectUri)
            .setState(userId)
            .build()
    }
    
    override fun exchangeCodeForTokens(
        authorizationCode: String,
        emailAddress: String,
    ): TokenInfo {
        val flow = createAuthorizationFlow()
        val tokenResponse = flow.newTokenRequest(authorizationCode)
            .setRedirectUri(emailProperty.gmailRedirectUri)
            .execute() as GoogleTokenResponse
        
        return TokenInfo(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            expiresInSeconds = tokenResponse.expiresInSeconds ?: 3600L,
        )
    }
    
    override fun refreshAccessToken(refreshToken: String): TokenInfo {
        val flow = createAuthorizationFlow()
        val credential = Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken("")
            .setRefreshToken(refreshToken)
        
        credential.refreshToken()
        
        return TokenInfo(
            accessToken = credential.accessToken,
            refreshToken = credential.refreshToken,
            expiresInSeconds = credential.expiresInSeconds ?: 3600L,
        )
    }
    
    override fun getEmails(
        emailAccount: EmailAccount,
        folderName: String?,
        maxResults: Int,
        pageToken: String?,
    ): EmailSyncResult {
        val gmail = createGmailService(emailAccount)
        
        val query = buildQuery(folderName)
        val request = gmail.users().messages().list("me")
            .setQ(query)
            .setMaxResults(maxResults.toLong())
        
        if (pageToken != null) {
            request.pageToken = pageToken
        }
        
        val response: ListMessagesResponse = request.execute()
        val messages = response.messages ?: emptyList()
        
        val emails = messages.mapNotNull { messageRef ->
            try {
                getEmailFromMessage(gmail, messageRef.id, emailAccount.identifier)
            } catch (e: Exception) {
                // 개별 메시지 처리 실패 시 로그 후 계속 진행
                null
            }
        }
        
        return EmailSyncResult(
            emails = emails,
            nextPageToken = response.nextPageToken,
            hasMore = response.nextPageToken != null,
        )
    }
    
    override fun getEmail(
        emailAccount: EmailAccount,
        messageId: String,
    ): Email? {
        return try {
            val gmail = createGmailService(emailAccount)
            getEmailFromMessage(gmail, messageId, emailAccount.identifier)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun markAsRead(
        emailAccount: EmailAccount,
        messageId: String,
    ) {
        val gmail = createGmailService(emailAccount)
        val modifyRequest = ModifyMessageRequest()
            .setRemoveLabelIds(listOf("UNREAD"))
        
        gmail.users().messages().modify("me", messageId, modifyRequest).execute()
    }
    
    override fun markAsUnread(
        emailAccount: EmailAccount,
        messageId: String,
    ) {
        val gmail = createGmailService(emailAccount)
        val modifyRequest = ModifyMessageRequest()
            .setAddLabelIds(listOf("UNREAD"))
        
        gmail.users().messages().modify("me", messageId, modifyRequest).execute()
    }
    
    private fun createAuthorizationFlow(): GoogleAuthorizationCodeFlow {
        val clientSecrets = GoogleClientSecrets().apply {
            web = GoogleClientSecrets.Details().apply {
                clientId = emailProperty.gmailClientId
                clientSecret = emailProperty.gmailClientSecret
            }
        }
        
        return GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            emailProperty.gmailScopes
        ).setAccessType("offline").build()
    }
    
    private fun createGmailService(emailAccount: EmailAccount): Gmail {
        val credential = Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(emailAccount.accessToken)
            .setRefreshToken(emailAccount.refreshToken)
        
        return Gmail.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("Whale Email Manager")
            .build()
    }
    
    private fun buildQuery(folderName: String?): String {
        return when (folderName?.uppercase()) {
            "INBOX" -> "in:inbox"
            "SENT" -> "in:sent"
            "DRAFT" -> "in:draft"
            "SPAM" -> "in:spam"
            "TRASH" -> "in:trash"
            else -> "in:inbox"
        }
    }
    
    private fun getEmailFromMessage(
        gmail: Gmail,
        messageId: String,
        accountIdentifier: UUID,
    ): Email {
        val message: Message = gmail.users().messages().get("me", messageId)
            .setFormat("full")
            .execute()
        
        val headers = message.payload?.headers ?: emptyList()
        val headerMap = headers.associate { it.name to it.value }
        
        return Email(
            identifier = UUID.randomUUID(),
            emailAccountIdentifier = accountIdentifier,
            messageId = messageId,
            threadId = message.threadId,
            subject = headerMap["Subject"],
            senderEmail = extractEmail(headerMap["From"]),
            senderName = extractName(headerMap["From"]),
            recipientEmails = parseEmailList(headerMap["To"]),
            ccEmails = parseEmailList(headerMap["Cc"]),
            bccEmails = parseEmailList(headerMap["Bcc"]),
            bodyText = extractTextBody(message),
            bodyHtml = extractHtmlBody(message),
            dateSent = parseDate(headerMap["Date"]),
            dateReceived = OffsetDateTime.now(),
            isRead = !message.labelIds?.contains("UNREAD") ?: true,
            isStarred = message.labelIds?.contains("STARRED") ?: false,
            isImportant = message.labelIds?.contains("IMPORTANT") ?: false,
            folderName = determineFolderName(message.labelIds),
            labels = message.labelIds ?: emptyList(),
            sizeBytes = message.sizeEstimate?.toLong(),
            hasAttachments = hasAttachments(message),
            createdDate = OffsetDateTime.now(),
            modifiedDate = OffsetDateTime.now(),
        )
    }
    
    // Helper methods would continue here...
    private fun extractEmail(fromHeader: String?): String? {
        if (fromHeader == null) return null
        val emailRegex = """[\w._%+-]+@[\w.-]+\.[A-Za-z]{2,}""".toRegex()
        return emailRegex.find(fromHeader)?.value
    }
    
    private fun extractName(fromHeader: String?): String? {
        if (fromHeader == null) return null
        val nameRegex = """^([^<]+)<""".toRegex()
        return nameRegex.find(fromHeader)?.groupValues?.get(1)?.trim()
    }
    
    private fun parseEmailList(emailHeader: String?): List<String> {
        if (emailHeader == null) return emptyList()
        val emailRegex = """[\w._%+-]+@[\w.-]+\.[A-Za-z]{2,}""".toRegex()
        return emailRegex.findAll(emailHeader).map { it.value }.toList()
    }
    
    private fun parseDate(dateHeader: String?): OffsetDateTime? {
        // Gmail API의 날짜 파싱 로직 구현
        return null // 임시로 null 반환
    }
    
    private fun extractTextBody(message: Message): String? {
        // 메시지에서 텍스트 본문 추출
        return null // 임시로 null 반환
    }
    
    private fun extractHtmlBody(message: Message): String? {
        // 메시지에서 HTML 본문 추출
        return null // 임시로 null 반환
    }
    
    private fun determineFolderName(labelIds: List<String>?): String? {
        return when {
            labelIds?.contains("INBOX") == true -> "INBOX"
            labelIds?.contains("SENT") == true -> "SENT"
            labelIds?.contains("DRAFT") == true -> "DRAFT"
            else -> "INBOX"
        }
    }
    
    private fun hasAttachments(message: Message): Boolean {
        // 첨부파일 존재 여부 확인
        return false // 임시로 false 반환
    }
}
