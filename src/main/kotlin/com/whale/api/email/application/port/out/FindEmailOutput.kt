package com.whale.api.email.application.port.out

import com.whale.api.email.domain.Email
import java.util.UUID

interface FindEmailOutput {
    fun findByIdentifier(identifier: UUID): Email?

    fun findByAccountIdentifier(
        accountIdentifier: UUID,
        folderName: String? = null,
        isRead: Boolean? = null,
        limit: Int = 50,
        offset: Int = 0,
    ): List<Email>

    fun findByMessageId(
        accountIdentifier: UUID,
        messageId: String,
    ): Email?

    fun searchEmails(
        accountIdentifier: UUID? = null,
        query: String,
        limit: Int = 50,
        offset: Int = 0,
    ): List<Email>

    fun existsByAccountIdentifierAndMessageId(
        accountIdentifier: UUID,
        messageId: String,
    ): Boolean
}
