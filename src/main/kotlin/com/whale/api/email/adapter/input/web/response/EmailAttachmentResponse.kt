package com.whale.api.email.adapter.input.web.response

import com.whale.api.email.domain.EmailAttachment
import java.time.OffsetDateTime
import java.util.UUID

data class EmailAttachmentResponse(
    val identifier: UUID,
    val emailIdentifier: UUID,
    val attachmentId: String,
    val filename: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val isInline: Boolean,
    val contentId: String?,
    val hasLocalFile: Boolean,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    companion object {
        fun from(attachment: EmailAttachment): EmailAttachmentResponse {
            return EmailAttachmentResponse(
                identifier = attachment.identifier,
                emailIdentifier = attachment.emailIdentifier,
                attachmentId = attachment.attachmentId,
                filename = attachment.filename,
                mimeType = attachment.mimeType,
                sizeBytes = attachment.sizeBytes,
                isInline = attachment.isInline,
                contentId = attachment.contentId,
                hasLocalFile = attachment.localFilePath != null,
                createdDate = attachment.createdDate,
                modifiedDate = attachment.modifiedDate,
            )
        }

        fun fromList(attachments: List<EmailAttachment>): List<EmailAttachmentResponse> {
            return attachments.map { from(it) }
        }
    }
}
