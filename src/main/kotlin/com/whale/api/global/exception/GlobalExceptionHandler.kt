package com.whale.api.global.exception

import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Access denied: ${ex.message} for request: ${request.getDescription(false)}" }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                path = request.getDescription(false).removePrefix("uri="),
                message = "접근 권한이 없습니다.",
            ),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        request: HttpServletRequest,
        ex: Exception,
    ): ResponseEntity<ErrorResponse> {
        val statusCode = ErrorCodeFactory.createErrorCode(ex)
        val path = request.requestURI
        val message = ex.message

        when (statusCode) {
            in 400..499 -> {
                logger.warn(
                    "Http request error. code={}, path={}, message={}, exType={}",
                    statusCode,
                    path,
                    message,
                    ex.javaClass.simpleName,
                )
            }
            in 500..599 -> {
                logger.error(
                    "Http request error. code={}, path={}, message={}, exType={}",
                    statusCode,
                    path,
                    message,
                    ex.javaClass.simpleName,
                )
            }
        }

        return ResponseEntity
            .status(statusCode)
            .body(
                ErrorResponse(
                    status = statusCode,
                    path = path,
                    message = message ?: "",
                ),
            )
    }
}
