package com.whale.api.email.adapter.output.persistence

import com.whale.api.email.adapter.output.persistence.entity.EmailAttachmentEntity.Companion.toEntity
import com.whale.api.email.adapter.output.persistence.repository.EmailAttachmentRepository
import com.whale.api.email.domain.EmailAttachment
import org.springframework.stereotype.Repository
import java.util.UUID

interface SaveEmailAttachmentOutput {
    fun save(emailAttachment: EmailAttachment): EmailAttachment
    fun saveAll(emailAttachments: List<EmailAttachment>): List<EmailAttachment>
}

interface FindEmailAttachmentOutput {
    fun findByEmailIdentifier(emailIdentifier: UUID): List<EmailAttachment>
    fun findByEmailIdentifierAndIsInline(
        emailIdentifier: UUID,
        isInline: Boolean,
    ): List<EmailAttachment>
    fun countByEmailIdentifier(emailIdentifier: UUID): Long
}

interface DeleteEmailAttachmentOutput {
    fun deleteByEmailIdentifier(emailIdentifier: UUID)
}

@Repository
class EmailAttachmentPersistenceAdapter(
    private val emailAttachmentRepository: EmailAttachmentRepository,
) : SaveEmailAttachmentOutput,
    FindEmailAttachmentOutput,
    DeleteEmailAttachmentOutput {
    
    override fun save(emailAttachment: EmailAttachment): EmailAttachment {
        return emailAttachmentRepository.save(emailAttachment.toEntity()).toDomain()
    }
    
    override fun saveAll(emailAttachments: List<EmailAttachment>): List<EmailAttachment> {
        val entities = emailAttachments.map { it.toEntity() }
        return emailAttachmentRepository.saveAll(entities).map { it.toDomain() }
    }
    
    override fun findByEmailIdentifier(emailIdentifier: UUID): List<EmailAttachment> {
        return emailAttachmentRepository.findByEmailIdentifier(emailIdentifier).map { it.toDomain() }
    }
    
    override fun findByEmailIdentifierAndIsInline(
        emailIdentifier: UUID,
        isInline: Boolean,
    ): List<EmailAttachment> {
        return emailAttachmentRepository.findByEmailIdentifierAndIsInline(emailIdentifier, isInline)
            .map { it.toDomain() }
    }
    
    override fun countByEmailIdentifier(emailIdentifier: UUID): Long {
        return emailAttachmentRepository.countByEmailIdentifier(emailIdentifier)
    }
    
    override fun deleteByEmailIdentifier(emailIdentifier: UUID) {
        emailAttachmentRepository.deleteByEmailIdentifier(emailIdentifier)
    }
}
