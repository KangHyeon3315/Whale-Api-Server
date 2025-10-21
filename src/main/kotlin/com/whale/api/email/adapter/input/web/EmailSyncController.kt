package com.whale.api.email.adapter.input.web

import com.whale.api.email.adapter.input.web.request.SyncEmailRequest
import com.whale.api.email.application.port.`in`.SyncEmailUseCase
import com.whale.api.global.annotation.RequireAuth
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/email/sync")
class EmailSyncController(
    private val syncEmailUseCase: SyncEmailUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @PostMapping("/all")
    fun syncAllAccounts(
        @Valid @RequestBody request: SyncEmailRequest,
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Starting sync for all accounts of user: ${request.userId}" }

        syncEmailUseCase.syncAllAccounts(request.userId.toString())

        logger.info { "Completed sync for all accounts of user: ${request.userId}" }
        return ResponseEntity.ok(mapOf("message" to "모든 계정 동기화가 완료되었습니다"))
    }

    @RequireAuth
    @PostMapping("/account/{accountId}")
    fun syncAccount(
        @PathVariable accountId: UUID,
        @RequestParam userId: UUID,
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Starting sync for account: $accountId, user: $userId" }

        syncEmailUseCase.syncAccount(userId.toString(), accountId)

        logger.info { "Completed sync for account: $accountId" }
        return ResponseEntity.ok(mapOf("message" to "계정 동기화가 완료되었습니다"))
    }

    @RequireAuth
    @PostMapping("/account/{accountId}/folder/{folderName}")
    fun syncAccountFolder(
        @PathVariable accountId: UUID,
        @PathVariable folderName: String,
        @RequestParam userId: UUID,
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Starting sync for account: $accountId, folder: $folderName, user: $userId" }

        syncEmailUseCase.syncAccountFolder(userId.toString(), accountId, folderName)

        logger.info { "Completed sync for account: $accountId, folder: $folderName" }
        return ResponseEntity.ok(mapOf("message" to "폴더 동기화가 완료되었습니다"))
    }
}
