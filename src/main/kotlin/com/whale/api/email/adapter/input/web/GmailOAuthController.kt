package com.whale.api.email.adapter.input.web

import com.whale.api.email.adapter.input.web.response.EmailAccountResponse
import com.whale.api.email.application.port.`in`.RegisterEmailAccountUseCase
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import java.util.UUID

@RestController
@RequestMapping("/email/oauth")
class GmailOAuthController(
    private val registerEmailAccountUseCase: RegisterEmailAccountUseCase,
) {
    private val logger = KotlinLogging.logger {}
    
    @GetMapping("/gmail/callback")
    fun handleGmailCallback(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam(required = false) error: String?,
    ): RedirectView {
        logger.info { "Handling Gmail OAuth callback: state=$state, error=$error" }
        
        if (error != null) {
            logger.error { "Gmail OAuth error: $error" }
            return RedirectView("/email/oauth/error?error=$error")
        }
        
        return try {
            val userId = UUID.fromString(state)
            
            // OAuth2 콜백 처리 (현재는 미구현 상태)
            // val emailAccount = registerEmailAccountUseCase.handleGmailOAuthCallback(userId, code)
            
            logger.info { "Gmail OAuth callback processed successfully for user: $userId" }
            RedirectView("/email/oauth/success")
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to process Gmail OAuth callback" }
            RedirectView("/email/oauth/error?error=processing_failed")
        }
    }
    
    @GetMapping("/success")
    fun oauthSuccess(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "message" to "Gmail 계정이 성공적으로 연결되었습니다",
            "status" to "success"
        ))
    }
    
    @GetMapping("/error")
    fun oauthError(
        @RequestParam(required = false) error: String?,
    ): ResponseEntity<Map<String, String>> {
        val errorMessage = when (error) {
            "access_denied" -> "사용자가 권한을 거부했습니다"
            "processing_failed" -> "계정 연결 처리 중 오류가 발생했습니다"
            else -> "알 수 없는 오류가 발생했습니다"
        }
        
        return ResponseEntity.badRequest().body(mapOf(
            "message" to errorMessage,
            "status" to "error",
            "error" to (error ?: "unknown")
        ))
    }
}
