package com.whale.api.email.adapter.input.web.response

import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import java.time.OffsetDateTime
import java.util.UUID

data class EmailAccountResponse(
    val identifier: UUID,
    val userId: UUID,
    val emailAddress: String,
    val provider: EmailProvider,
    val displayName: String?,
    val isActive: Boolean,
    val syncEnabled: Boolean,
    val lastSyncDate: OffsetDateTime?,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    companion object {
        fun from(emailAccount: EmailAccount): EmailAccountResponse {
            return EmailAccountResponse(
                identifier = emailAccount.identifier,
                userId = emailAccount.userId,
                emailAddress = emailAccount.emailAddress,
                provider = emailAccount.provider,
                displayName = emailAccount.displayName,
                isActive = emailAccount.isActive,
                syncEnabled = emailAccount.syncEnabled,
                lastSyncDate = emailAccount.lastSyncDate,
                createdDate = emailAccount.createdDate,
                modifiedDate = emailAccount.modifiedDate,
            )
        }
        
        fun fromList(emailAccounts: List<EmailAccount>): List<EmailAccountResponse> {
            return emailAccounts.map { from(it) }
        }
    }
}
