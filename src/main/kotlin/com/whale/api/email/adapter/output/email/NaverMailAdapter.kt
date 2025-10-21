package com.whale.api.email.adapter.output.email

import com.whale.api.email.application.port.out.EncryptionOutput
import com.whale.api.email.application.port.out.NaverMailProviderOutput
import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.property.EmailProperty
import jakarta.mail.Authenticator
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import java.util.UUID

@Component
class NaverMailAdapter(
    private val emailProperty: EmailProperty,
    private val encryptionOutput: EncryptionOutput,
) : NaverMailProviderOutput {
    
    override fun testConnection(
        emailAddress: String,
        password: String,
    ): Boolean {
        return try {
            val session = createImapSession(emailAddress, password)
            val store = session.getStore("imaps")
            store.connect(emailProperty.naverImapHost, emailProperty.naverImapPort, emailAddress, password)
            store.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getEmails(
        emailAccount: EmailAccount,
        folderName: String,
        maxResults: Int,
    ): List<Email> {
        val password = encryptionOutput.decrypt(emailAccount.encryptedPassword!!)
        val session = createImapSession(emailAccount.emailAddress, password)
        
        return try {
            val store = session.getStore("imaps")
            store.connect(emailProperty.naverImapHost, emailProperty.naverImapPort, emailAccount.emailAddress, password)
            
            val folder = store.getFolder(folderName)
            folder.open(Folder.READ_ONLY)
            
            val messageCount = folder.messageCount
            val startIndex = maxOf(1, messageCount - maxResults + 1)
            val messages = folder.getMessages(startIndex, messageCount)
            
            val emails = messages.mapNotNull { message ->
                try {
                    convertToEmail(message, emailAccount.identifier, folderName)
                } catch (e: Exception) {
                    // 개별 메시지 처리 실패 시 로그 후 계속 진행
                    null
                }
            }
            
            folder.close(false)
            store.close()
            
            emails.reversed() // 최신 메시지가 먼저 오도록
        } catch (e: Exception) {
            throw NaverMailException("Failed to fetch emails from Naver Mail", e)
        }
    }
    
    override fun getEmail(
        emailAccount: EmailAccount,
        messageId: String,
    ): Email? {
        val password = encryptionOutput.decrypt(emailAccount.encryptedPassword!!)
        val session = createImapSession(emailAccount.emailAddress, password)
        
        return try {
            val store = session.getStore("imaps")
            store.connect(emailProperty.naverImapHost, emailProperty.naverImapPort, emailAccount.emailAddress, password)
            
            val folder = store.getFolder("INBOX")
            folder.open(Folder.READ_ONLY)
            
            val messages = folder.messages
            val targetMessage = messages.find { message ->
                getMessageId(message) == messageId
            }
            
            val email = targetMessage?.let { convertToEmail(it, emailAccount.identifier, "INBOX") }
            
            folder.close(false)
            store.close()
            
            email
        } catch (e: Exception) {
            null
        }
    }
    
    override fun markAsRead(
        emailAccount: EmailAccount,
        messageId: String,
    ) {
        updateMessageFlags(emailAccount, messageId, Flags.Flag.SEEN, true)
    }
    
    override fun markAsUnread(
        emailAccount: EmailAccount,
        messageId: String,
    ) {
        updateMessageFlags(emailAccount, messageId, Flags.Flag.SEEN, false)
    }
    
    override fun getFolders(emailAccount: EmailAccount): List<String> {
        val password = encryptionOutput.decrypt(emailAccount.encryptedPassword!!)
        val session = createImapSession(emailAccount.emailAddress, password)
        
        return try {
            val store = session.getStore("imaps")
            store.connect(emailProperty.naverImapHost, emailProperty.naverImapPort, emailAccount.emailAddress, password)
            
            val folders = store.defaultFolder.list("*")
            val folderNames = folders.map { it.name }
            
            store.close()
            folderNames
        } catch (e: Exception) {
            // 기본 폴더 목록 반환
            listOf("INBOX", "SENT", "DRAFT", "TRASH", "SPAM")
        }
    }
    
    private fun createImapSession(emailAddress: String, password: String): Session {
        val props = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", emailProperty.naverImapHost)
            put("mail.imaps.port", emailProperty.naverImapPort.toString())
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.ssl.trust", "*")
            put("mail.imaps.connectiontimeout", "10000")
            put("mail.imaps.timeout", "10000")
        }
        
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailAddress, password)
            }
        })
    }
    
    private fun updateMessageFlags(
        emailAccount: EmailAccount,
        messageId: String,
        flag: Flags.Flag,
        set: Boolean,
    ) {
        val password = encryptionOutput.decrypt(emailAccount.encryptedPassword!!)
        val session = createImapSession(emailAccount.emailAddress, password)
        
        try {
            val store = session.getStore("imaps")
            store.connect(emailProperty.naverImapHost, emailProperty.naverImapPort, emailAccount.emailAddress, password)
            
            val folder = store.getFolder("INBOX")
            folder.open(Folder.READ_WRITE)
            
            val messages = folder.messages
            val targetMessage = messages.find { message ->
                getMessageId(message) == messageId
            }
            
            targetMessage?.setFlag(flag, set)
            
            folder.close(true)
            store.close()
        } catch (e: Exception) {
            throw NaverMailException("Failed to update message flags", e)
        }
    }
    
    private fun convertToEmail(
        message: Message,
        accountIdentifier: UUID,
        folderName: String,
    ): Email {
        return Email(
            identifier = UUID.randomUUID(),
            emailAccountIdentifier = accountIdentifier,
            messageId = getMessageId(message),
            threadId = null, // IMAP doesn't provide thread ID
            subject = message.subject,
            senderEmail = extractSenderEmail(message),
            senderName = extractSenderName(message),
            recipientEmails = extractRecipients(message, Message.RecipientType.TO),
            ccEmails = extractRecipients(message, Message.RecipientType.CC),
            bccEmails = extractRecipients(message, Message.RecipientType.BCC),
            bodyText = extractTextContent(message),
            bodyHtml = extractHtmlContent(message),
            dateSent = message.sentDate?.toInstant()?.atOffset(ZoneOffset.UTC),
            dateReceived = message.receivedDate?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            isRead = message.isSet(Flags.Flag.SEEN),
            isStarred = message.isSet(Flags.Flag.FLAGGED),
            isImportant = false, // IMAP doesn't have importance flag
            folderName = folderName,
            labels = emptyList(), // IMAP doesn't support labels like Gmail
            sizeBytes = message.size.toLong(),
            hasAttachments = hasAttachments(message),
            createdDate = OffsetDateTime.now(),
            modifiedDate = OffsetDateTime.now(),
        )
    }
    
    private fun getMessageId(message: Message): String {
        return message.getHeader("Message-ID")?.firstOrNull() ?: "unknown-${System.currentTimeMillis()}"
    }
    
    private fun extractSenderEmail(message: Message): String? {
        val from = message.from?.firstOrNull() as? InternetAddress
        return from?.address
    }
    
    private fun extractSenderName(message: Message): String? {
        val from = message.from?.firstOrNull() as? InternetAddress
        return from?.personal
    }
    
    private fun extractRecipients(message: Message, type: Message.RecipientType): List<String> {
        return message.getRecipients(type)?.mapNotNull { recipient ->
            (recipient as? InternetAddress)?.address
        } ?: emptyList()
    }
    
    private fun extractTextContent(message: Message): String? {
        return try {
            when {
                message.isMimeType("text/plain") -> message.content as? String
                message.isMimeType("multipart/*") -> {
                    val multipart = message.content as MimeMultipart
                    extractTextFromMultipart(multipart)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractHtmlContent(message: Message): String? {
        return try {
            when {
                message.isMimeType("text/html") -> message.content as? String
                message.isMimeType("multipart/*") -> {
                    val multipart = message.content as MimeMultipart
                    extractHtmlFromMultipart(multipart)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractTextFromMultipart(multipart: MimeMultipart): String? {
        for (i in 0 until multipart.count) {
            val part = multipart.getBodyPart(i)
            if (part.isMimeType("text/plain")) {
                return part.content as? String
            }
        }
        return null
    }
    
    private fun extractHtmlFromMultipart(multipart: MimeMultipart): String? {
        for (i in 0 until multipart.count) {
            val part = multipart.getBodyPart(i)
            if (part.isMimeType("text/html")) {
                return part.content as? String
            }
        }
        return null
    }
    
    private fun hasAttachments(message: Message): Boolean {
        return try {
            if (message.isMimeType("multipart/*")) {
                val multipart = message.content as MimeMultipart
                for (i in 0 until multipart.count) {
                    val part = multipart.getBodyPart(i)
                    if (part.disposition != null && 
                        (part.disposition.equals("ATTACHMENT", ignoreCase = true) ||
                         part.disposition.equals("INLINE", ignoreCase = true))) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}

class NaverMailException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
