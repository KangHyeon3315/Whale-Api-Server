package com.whale.api.email.adapter.input.web

import com.whale.api.email.adapter.input.web.response.EmailListResponse
import com.whale.api.email.adapter.input.web.response.EmailResponse
import com.whale.api.email.application.port.`in`.GetEmailUseCase
import com.whale.api.global.annotation.RequireAuth
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/email")
class EmailController(
    private val getEmailUseCase: GetEmailUseCase,
) {
    private val logger = KotlinLogging.logger {}
    
    @RequireAuth
    @GetMapping("/accounts/{accountId}/emails")
    fun getEmails(
        @PathVariable accountId: UUID,
        @RequestParam userId: UUID,
        @RequestParam(required = false) folderName: String?,
        @RequestParam(required = false) isRead: Boolean?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
    ): ResponseEntity<EmailListResponse> {
        logger.info { 
            "Getting emails for account: $accountId, user: $userId, folder: $folderName, " +
            "isRead: $isRead, limit: $limit, offset: $offset" 
        }
        
        val emails = getEmailUseCase.getEmails(
            userId = userId,
            accountId = accountId,
            folderName = folderName,
            isRead = isRead,
            limit = limit,
            offset = offset
        )
        
        // 다음 페이지 존재 여부 확인을 위해 limit + 1로 조회했다고 가정
        val hasNext = emails.size > limit
        val actualEmails = if (hasNext) emails.dropLast(1) else emails
        
        logger.info { "Found ${actualEmails.size} emails for account: $accountId" }
        return ResponseEntity.ok(
            EmailListResponse.from(
                emails = actualEmails,
                totalCount = actualEmails.size,
                hasNext = hasNext
            )
        )
    }
    
    @RequireAuth
    @GetMapping("/{emailId}")
    fun getEmail(
        @PathVariable emailId: UUID,
        @RequestParam userId: UUID,
    ): ResponseEntity<EmailResponse> {
        logger.info { "Getting email: $emailId for user: $userId" }
        
        val email = getEmailUseCase.getEmail(userId, emailId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(EmailResponse.from(email))
    }
    
    @RequireAuth
    @GetMapping("/search")
    fun searchEmails(
        @RequestParam userId: UUID,
        @RequestParam query: String,
        @RequestParam(required = false) accountId: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
    ): ResponseEntity<EmailListResponse> {
        logger.info { 
            "Searching emails for user: $userId, query: '$query', account: $accountId, " +
            "limit: $limit, offset: $offset" 
        }
        
        val emails = getEmailUseCase.searchEmails(
            userId = userId,
            accountId = accountId,
            query = query,
            limit = limit,
            offset = offset
        )
        
        // 다음 페이지 존재 여부 확인
        val hasNext = emails.size > limit
        val actualEmails = if (hasNext) emails.dropLast(1) else emails
        
        logger.info { "Found ${actualEmails.size} emails matching query: '$query'" }
        return ResponseEntity.ok(
            EmailListResponse.from(
                emails = actualEmails,
                totalCount = actualEmails.size,
                hasNext = hasNext
            )
        )
    }
}
