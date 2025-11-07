package com.whale.api.email.adapter.output.persistence.entity

import com.whale.api.email.domain.EmailAttachment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "email_attachment")
data class EmailAttachmentEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "email_identifier", nullable = false)
    val emailIdentifier: UUID,
    // Provider-specific attachment ID
    @Column(name = "attachment_id", nullable = false)
    val attachmentId: String,
    // Attachment info
    @Column(name = "filename", nullable = false)
    val filename: String,
    @Column(name = "mime_type", nullable = true)
    val mimeType: String?,
    @Column(name = "size_bytes", nullable = true)
    val sizeBytes: Long?,
    @Column(name = "content_id", nullable = true)
    val contentId: String?,
    // Storage info
    @Column(name = "local_file_path", nullable = true, columnDefinition = "TEXT")
    val localFilePath: String?,
    @Column(name = "checksum", nullable = true)
    val checksum: String?,
    @Column(name = "is_inline", nullable = false)
    val isInline: Boolean = false,
    @Column(name = "has_local_file", nullable = false)
    val hasLocalFile: Boolean = false,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    val modifiedDate: OffsetDateTime,
) {
    fun toDomain(): EmailAttachment {
        return EmailAttachment(
            identifier = this.identifier,
            emailIdentifier = this.emailIdentifier,
            attachmentId = this.attachmentId,
            filename = this.filename,
            mimeType = this.mimeType,
            sizeBytes = this.sizeBytes,
            contentId = this.contentId,
            localFilePath = this.localFilePath,
            checksum = this.checksum,
            isInline = this.isInline,
            hasLocalFile = this.hasLocalFile,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
        )
    }

    companion object {
        fun EmailAttachment.toEntity(): EmailAttachmentEntity {
            return EmailAttachmentEntity(
                identifier = this.identifier,
                emailIdentifier = this.emailIdentifier,
                attachmentId = this.attachmentId,
                filename = this.filename,
                mimeType = this.mimeType,
                sizeBytes = this.sizeBytes,
                contentId = this.contentId,
                localFilePath = this.localFilePath,
                checksum = this.checksum,
                isInline = this.isInline,
                hasLocalFile = this.hasLocalFile,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
            )
        }
    }
}
