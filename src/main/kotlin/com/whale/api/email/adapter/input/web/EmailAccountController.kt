package com.whale.api.email.adapter.input.web

import com.whale.api.email.adapter.input.web.request.RegisterEmailAccountRequest
import com.whale.api.email.adapter.input.web.response.EmailAccountResponse
import com.whale.api.email.adapter.input.web.response.GmailAuthUrlResponse
import com.whale.api.email.application.port.`in`.GetEmailUseCase
import com.whale.api.email.application.port.`in`.RegisterEmailAccountUseCase
import com.whale.api.global.annotation.RequireAuth
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/email/accounts")
class EmailAccountController(
    private val registerEmailAccountUseCase: RegisterEmailAccountUseCase,
    private val getEmailUseCase: GetEmailUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @PostMapping("/register")
    fun registerEmailAccount(
        @Valid @RequestBody request: RegisterEmailAccountRequest,
    ): ResponseEntity<EmailAccountResponse> {
        logger.info { "Registering email account: ${request.emailAddress}, provider: ${request.provider}" }

        request.validate()

        val emailAccount =
            when (request.provider) {
                com.whale.api.email.domain.EmailProvider.GMAIL -> {
                    registerEmailAccountUseCase.registerGmailAccount(request.toCommand())
                }
                com.whale.api.email.domain.EmailProvider.NAVER -> {
                    registerEmailAccountUseCase.registerNaverAccount(request.toCommand())
                }
            }

        logger.info { "Successfully registered email account: ${emailAccount.identifier}" }
        return ResponseEntity.ok(EmailAccountResponse.from(emailAccount))
    }

    @RequireAuth
    @GetMapping
    fun getEmailAccounts(
        @RequestParam userId: UUID,
    ): ResponseEntity<List<EmailAccountResponse>> {
        logger.info { "Getting email accounts for user: $userId" }

        val emailAccounts = getEmailUseCase.getEmailAccounts(userId.toString())

        logger.info { "Found ${emailAccounts.size} email accounts for user: $userId" }
        return ResponseEntity.ok(EmailAccountResponse.fromList(emailAccounts))
    }

    @RequireAuth
    @GetMapping("/{accountId}")
    fun getEmailAccount(
        @PathVariable accountId: UUID,
        @RequestParam userId: UUID,
    ): ResponseEntity<EmailAccountResponse> {
        logger.info { "Getting email account: $accountId for user: $userId" }

        val emailAccount =
            getEmailUseCase.getEmailAccount(userId.toString(), accountId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(EmailAccountResponse.from(emailAccount))
    }

    @RequireAuth
    @GetMapping("/gmail/auth-url")
    fun getGmailAuthUrl(
        @RequestParam userId: UUID,
    ): ResponseEntity<GmailAuthUrlResponse> {
        logger.info { "Getting Gmail auth URL for user: $userId" }

        val authUrl = registerEmailAccountUseCase.getGmailAuthUrl(userId.toString())

        return ResponseEntity.ok(GmailAuthUrlResponse.from(authUrl, userId.toString()))
    }
}
