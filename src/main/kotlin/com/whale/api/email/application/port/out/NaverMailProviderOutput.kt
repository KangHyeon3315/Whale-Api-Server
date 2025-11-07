package com.whale.api.email.application.port.out

import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount

interface NaverMailProviderOutput {
    fun testConnection(
        emailAddress: String,
        password: String,
    ): Boolean

    fun getEmails(
        emailAccount: EmailAccount,
        folderName: String = "INBOX",
        maxResults: Int = 50,
    ): List<Email>

    fun getEmail(
        emailAccount: EmailAccount,
        messageId: String,
    ): Email?

    fun markAsRead(
        emailAccount: EmailAccount,
        messageId: String,
    )

    fun markAsUnread(
        emailAccount: EmailAccount,
        messageId: String,
    )

    fun getFolders(emailAccount: EmailAccount): List<String>
}
