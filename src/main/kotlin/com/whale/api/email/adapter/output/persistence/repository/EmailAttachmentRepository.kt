package com.whale.api.email.adapter.output.persistence.repository

import com.whale.api.email.adapter.output.persistence.entity.EmailAttachmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmailAttachmentRepository : JpaRepository<EmailAttachmentEntity, UUID> {
    fun findByEmailIdentifier(emailIdentifier: UUID): List<EmailAttachmentEntity>

    fun findByEmailIdentifierAndIsInline(
        emailIdentifier: UUID,
        isInline: Boolean,
    ): List<EmailAttachmentEntity>

    fun countByEmailIdentifier(emailIdentifier: UUID): Long

    fun deleteByEmailIdentifier(emailIdentifier: UUID)
}
