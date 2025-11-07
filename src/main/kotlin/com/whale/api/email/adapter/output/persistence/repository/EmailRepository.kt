package com.whale.api.email.adapter.output.persistence.repository

import com.whale.api.email.adapter.output.persistence.entity.EmailEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EmailRepository : JpaRepository<EmailEntity, UUID> {
    fun findByEmailAccountIdentifierAndMessageId(
        emailAccountIdentifier: UUID,
        messageId: String,
    ): EmailEntity?

    fun findByEmailAccountIdentifier(
        emailAccountIdentifier: UUID,
        pageable: Pageable,
    ): List<EmailEntity>

    fun findByEmailAccountIdentifierAndFolderName(
        emailAccountIdentifier: UUID,
        folderName: String,
        pageable: Pageable,
    ): List<EmailEntity>

    fun findByEmailAccountIdentifierAndIsRead(
        emailAccountIdentifier: UUID,
        isRead: Boolean,
        pageable: Pageable,
    ): List<EmailEntity>

    fun findByEmailAccountIdentifierAndFolderNameAndIsRead(
        emailAccountIdentifier: UUID,
        folderName: String,
        isRead: Boolean,
        pageable: Pageable,
    ): List<EmailEntity>

    fun existsByEmailAccountIdentifierAndMessageId(
        emailAccountIdentifier: UUID,
        messageId: String,
    ): Boolean

    @Query(
        """
        SELECT e FROM EmailEntity e 
        WHERE (:accountIdentifier IS NULL OR e.emailAccountIdentifier = :accountIdentifier)
        AND (
            LOWER(e.subject) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(e.senderEmail) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(e.senderName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(e.bodyText) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY e.dateReceived DESC
    """,
    )
    fun searchEmails(
        @Param("accountIdentifier") accountIdentifier: UUID?,
        @Param("query") query: String,
        pageable: Pageable,
    ): List<EmailEntity>

    fun countByEmailAccountIdentifier(emailAccountIdentifier: UUID): Long

    fun countByEmailAccountIdentifierAndIsRead(
        emailAccountIdentifier: UUID,
        isRead: Boolean,
    ): Long
}
