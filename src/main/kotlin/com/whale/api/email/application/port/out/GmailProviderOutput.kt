package com.whale.api.email.application.port.out

import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount

interface GmailProviderOutput {
    fun getAuthorizationUrl(userId: String): String

    fun exchangeCodeForTokens(
        authorizationCode: String,
        emailAddress: String,
    ): TokenInfo

    fun refreshAccessToken(refreshToken: String): TokenInfo

    fun getEmails(
        emailAccount: EmailAccount,
        folderName: String? = null,
        maxResults: Int = 50,
        pageToken: String? = null,
    ): EmailSyncResult

    fun getEmail(
        emailAccount: EmailAccount,
        messageId: String,
    ): Email?

    fun markAsRead(
        emailAccount: EmailAccount,
        messageId: String,
    )

    fun markAsUnread(
        emailAccount: EmailAccount,
        messageId: String,
    )
}

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String?,
    val expiresInSeconds: Long,
)

data class EmailSyncResult(
    val emails: List<Email>,
    val nextPageToken: String?,
    val hasMore: Boolean,
)
