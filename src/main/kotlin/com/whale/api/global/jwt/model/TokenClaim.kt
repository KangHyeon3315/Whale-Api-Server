package com.whale.api.global.jwt.model

import com.whale.api.global.jwt.enums.AuthRole
import com.whale.api.global.jwt.enums.TokenType
import java.time.OffsetDateTime
import java.util.UUID

data class TokenClaim(
    val userIdentifier: UUID,
    val roles: List<AuthRole>,
    val type: TokenType,
    val issuedDate: OffsetDateTime,
    val expirationDate: OffsetDateTime
) {
    val isExpired: Boolean
        get() = OffsetDateTime.now().isAfter(expirationDate)

    val isValid: Boolean
        get() = !isExpired
}
