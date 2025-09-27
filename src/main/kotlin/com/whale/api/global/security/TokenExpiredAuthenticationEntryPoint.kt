package com.whale.api.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class TokenExpiredAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    private val logger = KotlinLogging.logger {}

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warn { "Token expired for request: ${request.requestURI} - ${authException.message}" }

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
