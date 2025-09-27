package com.whale.api.global.jwt

import com.whale.api.global.jwt.enums.AuthRole
import com.whale.api.global.jwt.enums.TokenType
import com.whale.api.global.jwt.model.Token
import com.whale.api.global.jwt.model.TokenClaim
import java.util.UUID

interface JwtCrypter {
    fun encrypt(
        userIdentifier: UUID,
        roles: List<AuthRole>,
        type: TokenType,
    ): Token

    fun decrypt(token: String): TokenClaim
}
