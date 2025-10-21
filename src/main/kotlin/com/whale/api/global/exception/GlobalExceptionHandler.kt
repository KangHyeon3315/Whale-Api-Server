package com.whale.api.global.exception

import com.whale.api.email.adapter.output.email.NaverMailException
import com.whale.api.email.adapter.output.encryption.EncryptionException
import com.whale.api.email.application.service.EmailAccountAlreadyExistsException
import com.whale.api.email.application.service.EmailAccountNotFoundException
import com.whale.api.email.application.service.EmailSyncException
import com.whale.api.email.application.service.InvalidEmailCredentialsException
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
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

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    path = request.getDescription(false).removePrefix("uri="),
                    message = "접근 권한이 없습니다.",
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors
        val message = fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn { "Validation error: $message for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    path = request.requestURI,
                    message = message,
                ),
            )
    }

    @ExceptionHandler(EmailAccountAlreadyExistsException::class)
    fun handleEmailAccountAlreadyExistsException(
        ex: EmailAccountAlreadyExistsException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Email account already exists: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    path = request.requestURI,
                    message = ex.message ?: "이미 등록된 이메일 계정입니다.",
                ),
            )
    }

    @ExceptionHandler(InvalidEmailCredentialsException::class)
    fun handleInvalidEmailCredentialsException(
        ex: InvalidEmailCredentialsException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid email credentials: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    path = request.requestURI,
                    message = ex.message ?: "이메일 인증 정보가 올바르지 않습니다.",
                ),
            )
    }

    @ExceptionHandler(EmailAccountNotFoundException::class)
    fun handleEmailAccountNotFoundException(
        ex: EmailAccountNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Email account not found: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    path = request.requestURI,
                    message = ex.message ?: "이메일 계정을 찾을 수 없습니다.",
                ),
            )
    }

    @ExceptionHandler(EmailSyncException::class)
    fun handleEmailSyncException(
        ex: EmailSyncException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Email sync error: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path = request.requestURI,
                    message = ex.message ?: "이메일 동기화 중 오류가 발생했습니다.",
                ),
            )
    }

    @ExceptionHandler(EncryptionException::class)
    fun handleEncryptionException(
        ex: EncryptionException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Encryption error: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path = request.requestURI,
                    message = "암호화 처리 중 오류가 발생했습니다.",
                ),
            )
    }

    @ExceptionHandler(NaverMailException::class)
    fun handleNaverMailException(
        ex: NaverMailException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Naver mail error: ${ex.message} for request: ${request.requestURI}" }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_GATEWAY.value(),
                    path = request.requestURI,
                    message = ex.message ?: "Naver 메일 서비스 연결 중 오류가 발생했습니다.",
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
                    ex,
                )
            }
            in 500..599 -> {
                logger.error(
                    "Http request error. code={}, path={}, message={}, exType={}",
                    statusCode,
                    path,
                    message,
                    ex.javaClass.simpleName,
                    ex,
                )
            }
        }

        return ResponseEntity
            .status(statusCode)
            .header("Content-Type", "application/json")
            .body(
                ErrorResponse(
                    status = statusCode,
                    path = path,
                    message = message ?: "",
                ),
            )
    }
}
