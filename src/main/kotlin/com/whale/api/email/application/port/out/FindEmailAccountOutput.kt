package com.whale.api.email.application.port.out

import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import java.util.UUID

interface FindEmailAccountOutput {
    fun findByUserIdAndEmailAddress(
        userId: String,
        emailAddress: String,
    ): EmailAccount?

    fun findByUserIdAndIdentifier(
        userId: String,
        identifier: UUID,
    ): EmailAccount?

    fun findAllByUserId(userId: String): List<EmailAccount>

    fun findAllActiveByUserId(userId: String): List<EmailAccount>

    fun findAllActive(): List<EmailAccount>

    fun findAllActiveByProvider(provider: EmailProvider): List<EmailAccount>

    fun findStaleAccounts(hours: Int): List<EmailAccount>

    fun findAccountsWithExpiringTokens(provider: EmailProvider, expiryThresholdHours: Int): List<EmailAccount>

    fun findAccountsWithLongExpiredTokens(provider: EmailProvider, expiredDays: Int): List<EmailAccount>
}
