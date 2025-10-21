package com.whale.api.email.domain

import java.time.OffsetDateTime
import java.util.UUID

data class EmailAttachment(
    val identifier: UUID,
    val emailIdentifier: UUID,

    // Attachment info from email provider
    val attachmentId: String, // Provider-specific attachment ID
    val filename: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val contentId: String?, // For inline attachments

    // Storage info
    val localFilePath: String?, // Local file storage path
    val checksum: String?, // SHA-256 checksum

    val isInline: Boolean = false,
    val hasLocalFile: Boolean = false,

    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    fun isImage(): Boolean = mimeType?.startsWith("image/") == true

    fun isDocument(): Boolean = mimeType?.let { type ->
        type.startsWith("application/") && (
            type.contains("pdf") ||
            type.contains("word") ||
            type.contains("excel") ||
            type.contains("powerpoint")
        )
    } == true

    fun getFileExtension(): String = filename.substringAfterLast('.', "")
}
