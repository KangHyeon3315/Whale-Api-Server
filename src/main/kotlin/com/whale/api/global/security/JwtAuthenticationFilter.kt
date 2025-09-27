package com.whale.api.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.whale.api.global.constants.AuthConstants
import com.whale.api.global.jwt.exceptions.TokenExpiredException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null) {
                val authenticationToken = JwtAuthenticationToken(token)
                val authentication = authenticationManager.authenticate(authenticationToken)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: TokenExpiredException) {
            logger.warn("JWT token expired: ${e.message}", e)
            SecurityContextHolder.clearContext()
            handleTokenExpired(request, response)
            return
        } catch (e: Exception) {
            logger.warn("JWT authentication failed: ${e.message ?: "unknown error"}", e)
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(AuthConstants.AUTHORIZATION)

        return if (authorizationHeader != null && authorizationHeader.startsWith(AuthConstants.BEARER_WITH_SPACE)) {
            authorizationHeader.substring(AuthConstants.BEARER_WITH_SPACE.length)
        } else {
            null
        }
    }

    private fun handleTokenExpired(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = mapOf(
            "error" to "Token Expired",
            "message" to "토큰이 만료되었습니다. 새로운 토큰을 발급받아 주세요.",
            "status" to HttpServletResponse.SC_UNAUTHORIZED,
            "path" to request.requestURI,
            "code" to "TOKEN_EXPIRED"
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
