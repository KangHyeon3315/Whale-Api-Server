package com.whale.api.email.application.port.`in`.command

import com.whale.api.email.domain.EmailProvider

data class RegisterEmailAccountCommand(
    val userId: String,
    val emailAddress: String,
    val provider: EmailProvider,
    val displayName: String?,
    // For OAuth2 (Gmail)
    val authorizationCode: String? = null,
    // For IMAP (Naver)
    val password: String? = null,
)
