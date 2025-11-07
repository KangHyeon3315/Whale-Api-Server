package com.whale.api.email.domain.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class EmailProperty(
    // Gmail OAuth2 settings
    @Value("\${email.gmail.client-id}")
    val gmailClientId: String,
    @Value("\${email.gmail.client-secret}")
    val gmailClientSecret: String,
    @Value("\${email.gmail.redirect-uri}")
    val gmailRedirectUri: String,
    // Naver IMAP/SMTP settings
    @Value("\${email.naver.imap-host}")
    val naverImapHost: String,
    @Value("\${email.naver.imap-port}")
    val naverImapPort: Int,
    @Value("\${email.naver.smtp-host}")
    val naverSmtpHost: String,
    @Value("\${email.naver.smtp-port}")
    val naverSmtpPort: Int,
    // Encryption settings
    @Value("\${email.encryption.secret-key}")
    val encryptionSecretKey: String,
) {
    // Gmail OAuth2 scopes
    val gmailScopes =
        listOf(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://www.googleapis.com/auth/gmail.compose",
        )

    // Default sync settings
    val defaultSyncIntervalMinutes = 15
    val maxEmailsPerSync = 100
    val attachmentBasePath = "/app/email/attachments"
}
