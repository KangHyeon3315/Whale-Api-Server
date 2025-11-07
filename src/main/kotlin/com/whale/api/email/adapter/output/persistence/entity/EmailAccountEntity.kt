package com.whale.api.email.adapter.output.persistence.entity

import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "email_account")
data class EmailAccountEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: String,
    @Column(name = "email_address", nullable = false)
    val emailAddress: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: EmailProvider,
    @Column(name = "display_name", nullable = true)
    val displayName: String?,
    // OAuth2 credentials (for Gmail)
    @Column(name = "access_token", nullable = true, columnDefinition = "TEXT")
    val accessToken: String?,
    @Column(name = "refresh_token", nullable = true, columnDefinition = "TEXT")
    val refreshToken: String?,
    @Column(name = "token_expiry", nullable = true)
    val tokenExpiry: OffsetDateTime?,
    // IMAP/SMTP credentials (for Naver, encrypted)
    @Column(name = "encrypted_password", nullable = true, columnDefinition = "TEXT")
    val encryptedPassword: String?,
    // Account settings
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    @Column(name = "sync_enabled", nullable = false)
    val syncEnabled: Boolean = true,
    @Column(name = "last_sync_date", nullable = true)
    val lastSyncDate: OffsetDateTime?,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    val modifiedDate: OffsetDateTime,
) {
    fun toDomain(): EmailAccount {
        return EmailAccount(
            identifier = this.identifier,
            userId = this.userId,
            emailAddress = this.emailAddress,
            provider = this.provider,
            displayName = this.displayName,
            accessToken = this.accessToken,
            refreshToken = this.refreshToken,
            tokenExpiry = this.tokenExpiry,
            encryptedPassword = this.encryptedPassword,
            isActive = this.isActive,
            syncEnabled = this.syncEnabled,
            lastSyncDate = this.lastSyncDate,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
        )
    }

    companion object {
        fun EmailAccount.toEntity(): EmailAccountEntity {
            return EmailAccountEntity(
                identifier = this.identifier,
                userId = this.userId,
                emailAddress = this.emailAddress,
                provider = this.provider,
                displayName = this.displayName,
                accessToken = this.accessToken,
                refreshToken = this.refreshToken,
                tokenExpiry = this.tokenExpiry,
                encryptedPassword = this.encryptedPassword,
                isActive = this.isActive,
                syncEnabled = this.syncEnabled,
                lastSyncDate = this.lastSyncDate,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
            )
        }
    }
}
