package com.whale.api.email.application.port.`in`

import com.whale.api.email.application.port.`in`.command.RegisterEmailAccountCommand
import com.whale.api.email.domain.EmailAccount

interface RegisterEmailAccountUseCase {
    fun registerGmailAccount(command: RegisterEmailAccountCommand): EmailAccount
    
    fun registerNaverAccount(command: RegisterEmailAccountCommand): EmailAccount
    
    fun getGmailAuthUrl(userId: String): String
    
    fun handleGmailOAuthCallback(
        userId: String,
        authorizationCode: String,
    ): EmailAccount
}
