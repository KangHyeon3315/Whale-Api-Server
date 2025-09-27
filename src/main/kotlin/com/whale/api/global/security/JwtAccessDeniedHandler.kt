package com.whale.api.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        logger.warn { "Access denied for request: ${request.requestURI} - ${accessDeniedException.message}" }

        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = mapOf(
            "error" to "Access Denied",
            "message" to "접근 권한이 없습니다.",
            "status" to HttpServletResponse.SC_FORBIDDEN,
            "path" to request.requestURI
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
