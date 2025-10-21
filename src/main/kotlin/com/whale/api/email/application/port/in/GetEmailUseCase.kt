package com.whale.api.email.application.port.`in`

import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount
import java.util.UUID

interface GetEmailUseCase {
    fun getEmailAccounts(userId: String): List<EmailAccount>
    
    fun getEmailAccount(
        userId: String,
        accountId: UUID,
    ): EmailAccount?
    
    fun getEmails(
        userId: String,
        accountId: UUID,
        folderName: String? = null,
        isRead: Boolean? = null,
        limit: Int = 50,
        offset: Int = 0,
    ): List<Email>
    
    fun getEmail(
        userId: String,
        emailId: UUID,
    ): Email?
    
    fun searchEmails(
        userId: String,
        accountId: UUID? = null,
        query: String,
        limit: Int = 50,
        offset: Int = 0,
    ): List<Email>
}
