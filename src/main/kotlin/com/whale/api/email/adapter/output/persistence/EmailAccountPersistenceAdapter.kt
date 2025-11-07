package com.whale.api.email.adapter.output.persistence

import com.whale.api.email.adapter.output.persistence.entity.EmailAccountEntity.Companion.toEntity
import com.whale.api.email.adapter.output.persistence.repository.EmailAccountRepository
import com.whale.api.email.application.port.out.FindEmailAccountOutput
import com.whale.api.email.application.port.out.SaveEmailAccountOutput
import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class EmailAccountPersistenceAdapter(
    private val emailAccountRepository: EmailAccountRepository,
) : SaveEmailAccountOutput,
    FindEmailAccountOutput {
    override fun save(emailAccount: EmailAccount): EmailAccount {
        return emailAccountRepository.save(emailAccount.toEntity()).toDomain()
    }

    override fun findByUserIdAndEmailAddress(
        userId: String,
        emailAddress: String,
    ): EmailAccount? {
        return emailAccountRepository.findByUserIdAndEmailAddress(userId, emailAddress)?.toDomain()
    }

    override fun findByUserIdAndIdentifier(
        userId: String,
        identifier: UUID,
    ): EmailAccount? {
        return emailAccountRepository.findById(identifier)
            .orElse(null)
            ?.takeIf { it.userId == userId }
            ?.toDomain()
    }

    override fun findAllByUserId(userId: String): List<EmailAccount> {
        return emailAccountRepository.findAllByUserId(userId).map { it.toDomain() }
    }

    override fun findAllActiveByUserId(userId: String): List<EmailAccount> {
        return emailAccountRepository.findAllByUserIdAndIsActive(userId, true).map { it.toDomain() }
    }

    override fun findAllActive(): List<EmailAccount> {
        return emailAccountRepository.findAllByIsActive(true).map { it.toDomain() }
    }

    override fun findAllActiveByProvider(provider: EmailProvider): List<EmailAccount> {
        return emailAccountRepository.findAllByProviderAndIsActive(provider, true).map { it.toDomain() }
    }

    override fun findStaleAccounts(hours: Int): List<EmailAccount> {
        val threshold = OffsetDateTime.now().minusHours(hours.toLong())
        return emailAccountRepository.findStaleAccounts(threshold).map { it.toDomain() }
    }

    override fun findAccountsWithExpiringTokens(
        provider: EmailProvider,
        expiryThresholdHours: Int,
    ): List<EmailAccount> {
        val threshold = OffsetDateTime.now().plusHours(expiryThresholdHours.toLong())
        return emailAccountRepository.findAccountsWithExpiringTokens(provider, threshold).map { it.toDomain() }
    }

    override fun findAccountsWithLongExpiredTokens(
        provider: EmailProvider,
        expiredDays: Int,
    ): List<EmailAccount> {
        val threshold = OffsetDateTime.now().minusDays(expiredDays.toLong())
        return emailAccountRepository.findAccountsWithLongExpiredTokens(provider, threshold).map { it.toDomain() }
    }
}
