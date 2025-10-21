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
    
    // Attachment info
    @Column(name = "filename", nullable = false)
    val filename: String,
    
    @Column(name = "content_type", nullable = true)
    val contentType: String?,
    
    @Column(name = "size_bytes", nullable = true)
    val sizeBytes: Long?,
    
    @Column(name = "content_id", nullable = true)
    val contentId: String?,
    
    // Storage info
    @Column(name = "stored_path", nullable = true, columnDefinition = "TEXT")
    val storedPath: String?,
    
    @Column(name = "checksum", nullable = true)
    val checksum: String?,
    
    @Column(name = "is_inline", nullable = false)
    val isInline: Boolean = false,
    
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
) {
    fun toDomain(): EmailAttachment {
        return EmailAttachment(
            identifier = this.identifier,
            emailIdentifier = this.emailIdentifier,
            filename = this.filename,
            contentType = this.contentType,
            sizeBytes = this.sizeBytes,
            contentId = this.contentId,
            storedPath = this.storedPath,
            checksum = this.checksum,
            isInline = this.isInline,
            createdDate = this.createdDate,
        )
    }

    companion object {
        fun EmailAttachment.toEntity(): EmailAttachmentEntity {
            return EmailAttachmentEntity(
                identifier = this.identifier,
                emailIdentifier = this.emailIdentifier,
                filename = this.filename,
                contentType = this.contentType,
                sizeBytes = this.sizeBytes,
                contentId = this.contentId,
                storedPath = this.storedPath,
                checksum = this.checksum,
                isInline = this.isInline,
                createdDate = this.createdDate,
            )
        }
    }
}
