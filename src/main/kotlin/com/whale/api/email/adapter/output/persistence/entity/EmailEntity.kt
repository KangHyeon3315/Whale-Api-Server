package com.whale.api.email.adapter.output.persistence.entity

import com.whale.api.email.domain.Email
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "email")
data class EmailEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    
    @Column(name = "email_account_identifier", nullable = false)
    val emailAccountIdentifier: UUID,
    
    // Email identifiers
    @Column(name = "message_id", nullable = false)
    val messageId: String,
    
    @Column(name = "thread_id", nullable = true)
    val threadId: String?,
    
    // Email headers
    @Column(name = "subject", nullable = true, columnDefinition = "TEXT")
    val subject: String?,
    
    @Column(name = "sender_email", nullable = true)
    val senderEmail: String?,
    
    @Column(name = "sender_name", nullable = true)
    val senderName: String?,
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "recipient_emails", nullable = true, columnDefinition = "TEXT[]")
    val recipientEmails: Array<String>?,
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cc_emails", nullable = true, columnDefinition = "TEXT[]")
    val ccEmails: Array<String>?,
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "bcc_emails", nullable = true, columnDefinition = "TEXT[]")
    val bccEmails: Array<String>?,
    
    // Email content
    @Column(name = "body_text", nullable = true, columnDefinition = "TEXT")
    val bodyText: String?,
    
    @Column(name = "body_html", nullable = true, columnDefinition = "TEXT")
    val bodyHtml: String?,
    
    // Email metadata
    @Column(name = "date_sent", nullable = true)
    val dateSent: OffsetDateTime?,
    
    @Column(name = "date_received", nullable = false)
    val dateReceived: OffsetDateTime,
    
    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,
    
    @Column(name = "is_starred", nullable = false)
    val isStarred: Boolean = false,
    
    @Column(name = "is_important", nullable = false)
    val isImportant: Boolean = false,
    
    // Folder/Label information
    @Column(name = "folder_name", nullable = true)
    val folderName: String?,
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "labels", nullable = true, columnDefinition = "TEXT[]")
    val labels: Array<String>?,
    
    // Email size and attachments
    @Column(name = "size_bytes", nullable = true)
    val sizeBytes: Long?,
    
    @Column(name = "has_attachments", nullable = false)
    val hasAttachments: Boolean = false,
    
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    
    @Column(name = "modified_date", nullable = false)
    val modifiedDate: OffsetDateTime,
) {
    fun toDomain(): Email {
        return Email(
            identifier = this.identifier,
            emailAccountIdentifier = this.emailAccountIdentifier,
            messageId = this.messageId,
            threadId = this.threadId,
            subject = this.subject,
            senderEmail = this.senderEmail,
            senderName = this.senderName,
            recipientEmails = this.recipientEmails?.toList() ?: emptyList(),
            ccEmails = this.ccEmails?.toList() ?: emptyList(),
            bccEmails = this.bccEmails?.toList() ?: emptyList(),
            bodyText = this.bodyText,
            bodyHtml = this.bodyHtml,
            dateSent = this.dateSent,
            dateReceived = this.dateReceived,
            isRead = this.isRead,
            isStarred = this.isStarred,
            isImportant = this.isImportant,
            folderName = this.folderName,
            labels = this.labels?.toList() ?: emptyList(),
            sizeBytes = this.sizeBytes,
            hasAttachments = this.hasAttachments,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
        )
    }

    companion object {
        fun Email.toEntity(): EmailEntity {
            return EmailEntity(
                identifier = this.identifier,
                emailAccountIdentifier = this.emailAccountIdentifier,
                messageId = this.messageId,
                threadId = this.threadId,
                subject = this.subject,
                senderEmail = this.senderEmail,
                senderName = this.senderName,
                recipientEmails = if (this.recipientEmails.isNotEmpty()) this.recipientEmails.toTypedArray() else null,
                ccEmails = if (this.ccEmails.isNotEmpty()) this.ccEmails.toTypedArray() else null,
                bccEmails = if (this.bccEmails.isNotEmpty()) this.bccEmails.toTypedArray() else null,
                bodyText = this.bodyText,
                bodyHtml = this.bodyHtml,
                dateSent = this.dateSent,
                dateReceived = this.dateReceived,
                isRead = this.isRead,
                isStarred = this.isStarred,
                isImportant = this.isImportant,
                folderName = this.folderName,
                labels = if (this.labels.isNotEmpty()) this.labels.toTypedArray() else null,
                sizeBytes = this.sizeBytes,
                hasAttachments = this.hasAttachments,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
            )
        }
    }
}
