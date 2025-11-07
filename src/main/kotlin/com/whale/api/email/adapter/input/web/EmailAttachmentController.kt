package com.whale.api.email.adapter.input.web

import com.whale.api.email.adapter.input.web.response.EmailAttachmentResponse
import com.whale.api.email.application.service.EmailAttachmentService
import com.whale.api.global.annotation.RequireAuth
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/email/attachments")
class EmailAttachmentController(
    private val emailAttachmentService: EmailAttachmentService,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @GetMapping("/email/{emailId}")
    fun getAttachmentsByEmail(
        @PathVariable emailId: UUID,
        @RequestParam userId: UUID,
    ): ResponseEntity<List<EmailAttachmentResponse>> {
        logger.info { "Getting attachments for email: $emailId, user: $userId" }

        val attachments = emailAttachmentService.getAttachmentsByEmail(userId.toString(), emailId)

        logger.info { "Found ${attachments.size} attachments for email: $emailId" }
        return ResponseEntity.ok(EmailAttachmentResponse.fromList(attachments))
    }

    @RequireAuth
    @GetMapping("/{attachmentId}")
    fun getAttachment(
        @PathVariable attachmentId: UUID,
        @RequestParam userId: UUID,
    ): ResponseEntity<EmailAttachmentResponse> {
        logger.info { "Getting attachment: $attachmentId for user: $userId" }

        val attachment =
            emailAttachmentService.getAttachment(userId.toString(), attachmentId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(EmailAttachmentResponse.from(attachment))
    }

    @RequireAuth
    @GetMapping("/{attachmentId}/download")
    fun downloadAttachment(
        @PathVariable attachmentId: UUID,
        @RequestParam userId: UUID,
        @RequestParam emailId: UUID,
    ): ResponseEntity<ByteArrayResource> {
        logger.info { "Downloading attachment: $attachmentId for email: $emailId, user: $userId" }

        // 첨부파일 정보 조회
        val attachment =
            emailAttachmentService.getAttachment(userId.toString(), attachmentId)
                ?: return ResponseEntity.notFound().build()

        // 첨부파일 데이터 다운로드
        val attachmentData =
            emailAttachmentService.downloadAttachment(
                userId.toString(),
                emailId,
                attachment.attachmentId,
            ) ?: return ResponseEntity.notFound().build()

        val resource = ByteArrayResource(attachmentData)

        logger.info { "Successfully downloaded attachment: ${attachment.filename}" }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${attachment.filename}\"")
            .header(HttpHeaders.CONTENT_TYPE, attachment.mimeType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_LENGTH, attachmentData.size.toString())
            .body(resource)
    }

    @RequireAuth
    @GetMapping("/{attachmentId}/preview")
    fun previewAttachment(
        @PathVariable attachmentId: UUID,
        @RequestParam userId: UUID,
        @RequestParam emailId: UUID,
    ): ResponseEntity<ByteArrayResource> {
        logger.info { "Previewing attachment: $attachmentId for email: $emailId, user: $userId" }

        // 첨부파일 정보 조회
        val attachment =
            emailAttachmentService.getAttachment(userId.toString(), attachmentId)
                ?: return ResponseEntity.notFound().build()

        // 미리보기 가능한 파일 타입 확인
        if (!isPreviewableType(attachment.mimeType)) {
            return ResponseEntity.badRequest().build()
        }

        // 첨부파일 데이터 다운로드
        val attachmentData =
            emailAttachmentService.downloadAttachment(
                userId.toString(),
                emailId,
                attachment.attachmentId,
            ) ?: return ResponseEntity.notFound().build()

        val resource = ByteArrayResource(attachmentData)

        logger.info { "Successfully previewed attachment: ${attachment.filename}" }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, attachment.mimeType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_LENGTH, attachmentData.size.toString())
            .header("X-Content-Type-Options", "nosniff")
            .body(resource)
    }

    private fun isPreviewableType(mimeType: String?): Boolean {
        if (mimeType == null) return false

        return mimeType.startsWith("image/") ||
            mimeType.startsWith("text/") ||
            mimeType == "application/pdf" ||
            mimeType.startsWith("video/") ||
            mimeType.startsWith("audio/")
    }
}
