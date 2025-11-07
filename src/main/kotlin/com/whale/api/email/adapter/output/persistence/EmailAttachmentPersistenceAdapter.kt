package com.whale.api.email.adapter.output.persistence

import com.whale.api.email.adapter.output.persistence.entity.EmailAttachmentEntity.Companion.toEntity
import com.whale.api.email.adapter.output.persistence.repository.EmailAttachmentRepository
import com.whale.api.email.application.port.out.DeleteEmailAttachmentOutput
import com.whale.api.email.application.port.out.FindEmailAttachmentOutput
import com.whale.api.email.application.port.out.SaveEmailAttachmentOutput
import com.whale.api.email.domain.EmailAttachment
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

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

    override fun findByIdentifier(identifier: UUID): EmailAttachment? {
        return emailAttachmentRepository.findById(identifier).orElse(null)?.toDomain()
    }

    override fun findAllByEmailIdentifier(emailIdentifier: UUID): List<EmailAttachment> {
        return emailAttachmentRepository.findByEmailIdentifier(emailIdentifier).map { it.toDomain() }
    }

    override fun findByEmailIdentifierAndAttachmentId(
        emailIdentifier: UUID,
        attachmentId: String,
    ): EmailAttachment? {
        return emailAttachmentRepository.findByEmailIdentifier(emailIdentifier)
            .find { it.attachmentId == attachmentId }
            ?.toDomain()
    }

    override fun findOldAttachments(cutoffDate: OffsetDateTime): List<EmailAttachment> {
        return emailAttachmentRepository.findAll()
            .filter { it.createdDate.isBefore(cutoffDate) }
            .map { it.toDomain() }
    }

    override fun deleteByEmailIdentifier(emailIdentifier: UUID) {
        emailAttachmentRepository.deleteByEmailIdentifier(emailIdentifier)
    }
}
