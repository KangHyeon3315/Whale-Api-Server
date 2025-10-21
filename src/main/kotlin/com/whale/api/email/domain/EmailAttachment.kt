package com.whale.api.email.domain

import java.time.OffsetDateTime
import java.util.UUID

data class EmailAttachment(
    val identifier: UUID,
    val emailIdentifier: UUID,
    
    // Attachment info
    val filename: String,
    val contentType: String?,
    val sizeBytes: Long?,
    val contentId: String?, // For inline attachments
    
    // Storage info
    val storedPath: String?,
    val checksum: String?, // SHA-256 checksum
    
    val isInline: Boolean = false,
    
    val createdDate: OffsetDateTime,
) {
    fun isImage(): Boolean = contentType?.startsWith("image/") == true
    
    fun isDocument(): Boolean = contentType?.let { type ->
        type.startsWith("application/") && (
            type.contains("pdf") ||
            type.contains("word") ||
            type.contains("excel") ||
            type.contains("powerpoint")
        )
    } == true
    
    fun getFileExtension(): String = filename.substringAfterLast('.', "")
}
