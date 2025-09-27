package com.whale.api.global.security

import com.whale.api.global.jwt.JwtCrypter
import com.whale.api.global.jwt.exceptions.TokenExpiredException
import com.whale.api.global.jwt.exceptions.UnauthorizedException
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationProvider(
    private val jwtCrypter: JwtCrypter,
) : AuthenticationProvider {
    private val logger = KotlinLogging.logger {}

    override fun authenticate(authentication: Authentication): Authentication {
        val jwtToken = authentication as JwtAuthenticationToken
        val token = jwtToken.getToken()

        return try {
            val tokenClaim = jwtCrypter.decrypt(token)

            if (tokenClaim.isExpired) {
                logger.warn { "JWT token is expired for user: ${tokenClaim.userIdentifier}" }
                throw TokenExpiredException()
            }

            JwtAuthenticationToken(
                userIdentifier = tokenClaim.userIdentifier,
                roles = tokenClaim.roles,
                token = token,
            )
        } catch (e: TokenExpiredException) {
            // TokenExpiredException은 그대로 다시 throw하여 JwtAuthenticationFilter에서 처리하도록 함
            throw e
        } catch (e: Exception) {
            logger.warn { "JWT authentication failed for token: ${token.take(10)}... - ${e.message}" }
            throw UnauthorizedException()
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
