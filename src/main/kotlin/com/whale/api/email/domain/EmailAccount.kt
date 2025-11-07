package com.whale.api.email.domain

import java.time.OffsetDateTime
import java.util.UUID

data class EmailAccount(
    val identifier: UUID,
    val userId: String,
    val emailAddress: String,
    val provider: EmailProvider,
    val displayName: String?,
    // OAuth2 credentials (for Gmail)
    val accessToken: String?,
    val refreshToken: String?,
    val tokenExpiry: OffsetDateTime?,
    // IMAP/SMTP credentials (for Naver, encrypted)
    val encryptedPassword: String?,
    // Account settings
    val isActive: Boolean = true,
    val syncEnabled: Boolean = true,
    val lastSyncDate: OffsetDateTime?,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    fun isOAuthAccount(): Boolean = provider.supportsOAuth && accessToken != null

    fun isImapAccount(): Boolean = provider.supportsImap && encryptedPassword != null

    fun needsTokenRefresh(): Boolean = tokenExpiry?.isBefore(OffsetDateTime.now().plusMinutes(5)) == true
}
