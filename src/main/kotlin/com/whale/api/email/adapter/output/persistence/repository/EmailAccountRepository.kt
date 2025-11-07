package com.whale.api.email.adapter.output.persistence.repository

import com.whale.api.email.adapter.output.persistence.entity.EmailAccountEntity
import com.whale.api.email.domain.EmailProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface EmailAccountRepository : JpaRepository<EmailAccountEntity, UUID> {
    fun findByUserIdAndEmailAddress(
        userId: String,
        emailAddress: String,
    ): EmailAccountEntity?

    fun findAllByUserId(userId: String): List<EmailAccountEntity>

    fun findAllByUserIdAndIsActive(
        userId: String,
        isActive: Boolean,
    ): List<EmailAccountEntity>

    fun findAllByUserIdAndProvider(
        userId: String,
        provider: EmailProvider,
    ): List<EmailAccountEntity>

    fun existsByUserIdAndEmailAddress(
        userId: String,
        emailAddress: String,
    ): Boolean

    fun findAllByIsActive(isActive: Boolean): List<EmailAccountEntity>

    fun findAllByProviderAndIsActive(
        provider: EmailProvider,
        isActive: Boolean,
    ): List<EmailAccountEntity>

    @Query(
        """
        SELECT e FROM EmailAccountEntity e
        WHERE e.isActive = true
        AND e.syncEnabled = true
        AND (e.lastSyncDate IS NULL OR e.lastSyncDate < :threshold)
    """,
    )
    fun findStaleAccounts(
        @Param("threshold") threshold: OffsetDateTime,
    ): List<EmailAccountEntity>

    @Query(
        """
        SELECT e FROM EmailAccountEntity e
        WHERE e.provider = :provider
        AND e.isActive = true
        AND e.tokenExpiry IS NOT NULL
        AND e.tokenExpiry < :threshold
    """,
    )
    fun findAccountsWithExpiringTokens(
        @Param("provider") provider: EmailProvider,
        @Param("threshold") threshold: OffsetDateTime,
    ): List<EmailAccountEntity>

    @Query(
        """
        SELECT e FROM EmailAccountEntity e
        WHERE e.provider = :provider
        AND e.tokenExpiry IS NOT NULL
        AND e.tokenExpiry < :threshold
    """,
    )
    fun findAccountsWithLongExpiredTokens(
        @Param("provider") provider: EmailProvider,
        @Param("threshold") threshold: OffsetDateTime,
    ): List<EmailAccountEntity>
}
