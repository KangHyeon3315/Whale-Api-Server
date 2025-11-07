package com.whale.api.email.adapter.input.web.response

import com.whale.api.email.domain.Email
import java.time.OffsetDateTime
import java.util.UUID

data class EmailResponse(
    val identifier: UUID,
    val emailAccountIdentifier: UUID,
    val messageId: String,
    val threadId: String?,
    val subject: String?,
    val senderEmail: String?,
    val senderName: String?,
    val recipientEmails: List<String>,
    val ccEmails: List<String>,
    val bccEmails: List<String>,
    val bodyText: String?,
    val bodyHtml: String?,
    val dateSent: OffsetDateTime?,
    val dateReceived: OffsetDateTime,
    val isRead: Boolean,
    val isStarred: Boolean,
    val isImportant: Boolean,
    val folderName: String?,
    val labels: List<String>,
    val sizeBytes: Long?,
    val hasAttachments: Boolean,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    companion object {
        fun from(email: Email): EmailResponse {
            return EmailResponse(
                identifier = email.identifier,
                emailAccountIdentifier = email.emailAccountIdentifier,
                messageId = email.messageId,
                threadId = email.threadId,
                subject = email.subject,
                senderEmail = email.senderEmail,
                senderName = email.senderName,
                recipientEmails = email.recipientEmails,
                ccEmails = email.ccEmails,
                bccEmails = email.bccEmails,
                bodyText = email.bodyText,
                bodyHtml = email.bodyHtml,
                dateSent = email.dateSent,
                dateReceived = email.dateReceived,
                isRead = email.isRead,
                isStarred = email.isStarred,
                isImportant = email.isImportant,
                folderName = email.folderName,
                labels = email.labels,
                sizeBytes = email.sizeBytes,
                hasAttachments = email.hasAttachments,
                createdDate = email.createdDate,
                modifiedDate = email.modifiedDate,
            )
        }

        fun fromList(emails: List<Email>): List<EmailResponse> {
            return emails.map { from(it) }
        }
    }
}
