package com.whale.api.email.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Email(
    val identifier: UUID,
    val emailAccountIdentifier: UUID,
    // Email identifiers
    val messageId: String,
    val threadId: String?,
    // Email headers
    val subject: String?,
    val senderEmail: String?,
    val senderName: String?,
    val recipientEmails: List<String> = emptyList(),
    val ccEmails: List<String> = emptyList(),
    val bccEmails: List<String> = emptyList(),
    // Email content
    val bodyText: String?,
    val bodyHtml: String?,
    // Email metadata
    val dateSent: OffsetDateTime?,
    val dateReceived: OffsetDateTime,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isImportant: Boolean = false,
    // Folder/Label information
    val folderName: String?,
    val labels: List<String> = emptyList(),
    // Email size and attachments
    val sizeBytes: Long?,
    val hasAttachments: Boolean = false,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
) {
    fun isInbox(): Boolean = folderName?.uppercase() == "INBOX"

    fun isSent(): Boolean = folderName?.uppercase() == "SENT"

    fun isDraft(): Boolean = folderName?.uppercase() == "DRAFT"

    fun hasLabel(label: String): Boolean = labels.contains(label)
}
