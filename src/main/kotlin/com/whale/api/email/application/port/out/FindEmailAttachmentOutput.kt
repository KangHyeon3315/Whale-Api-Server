package com.whale.api.email.application.port.out

import com.whale.api.email.domain.EmailAttachment
import java.time.OffsetDateTime
import java.util.UUID

interface FindEmailAttachmentOutput {
    fun findByIdentifier(identifier: UUID): EmailAttachment?
    fun findAllByEmailIdentifier(emailIdentifier: UUID): List<EmailAttachment>
    fun findByEmailIdentifierAndAttachmentId(
        emailIdentifier: UUID,
        attachmentId: String,
    ): EmailAttachment?
    fun findOldAttachments(cutoffDate: OffsetDateTime): List<EmailAttachment>
}
