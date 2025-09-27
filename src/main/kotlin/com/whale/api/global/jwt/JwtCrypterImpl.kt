package com.whale.api.global.jwt

import com.whale.api.global.jwt.enums.AuthRole
import com.whale.api.global.jwt.enums.TokenType
import com.whale.api.global.jwt.exceptions.UnauthorizedException
import com.whale.api.global.jwt.model.Token
import com.whale.api.global.jwt.model.TokenClaim
import com.whale.api.global.jwt.property.JwtProperty
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID

@Component
class JwtCrypterImpl(
    private val jwtProperty: JwtProperty,
) : JwtCrypter {
    override fun encrypt(
        userIdentifier: UUID,
        roles: List<AuthRole>,
        type: TokenType,
    ): Token {
        val now = OffsetDateTime.now()
        val expirationDate = now.plus(type.validDuration)

        return Jwts.builder()
            .subject(userIdentifier.toString())
            .claim("roles", roles.joinToString { it.name })
            .claim("type", type.name)
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(expirationDate.toInstant()))
            .signWith(jwtProperty.secret)
            .compact()
            .let { Token(it, expirationDate) }
    }

    override fun decrypt(token: String): TokenClaim {
        return try {
            val claims: Claims =
                Jwts.parser()
                    .verifyWith(jwtProperty.secret)
                    .clockSkewSeconds(365 * 24 * 60 * 60) // 1년 정도의 여유 (만료 체크 사실상 비활성화)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            TokenClaim(
                userIdentifier = UUID.fromString(claims.subject),
                roles = (claims["roles"] as String).split(",").map { AuthRole.valueOf(it.trim()) }.distinct(),
                type = TokenType.valueOf(claims["type"] as String),
                issuedDate = OffsetDateTime.ofInstant(claims.issuedAt.toInstant(), ZoneId.systemDefault()),
                expirationDate = OffsetDateTime.ofInstant(claims.expiration.toInstant(), ZoneId.systemDefault()),
            )
        } catch (e: JwtException) {
            throw UnauthorizedException()
        }
    }
}
