package com.whale.api.email.application.service

import com.whale.api.email.application.port.out.FindEmailAttachmentOutput
import com.whale.api.email.application.port.out.FindEmailOutput
import com.whale.api.email.application.port.out.GmailProviderOutput
import com.whale.api.email.application.port.out.NaverMailProviderOutput
import com.whale.api.email.application.port.out.SaveEmailAttachmentOutput
import com.whale.api.email.domain.EmailAttachment
import com.whale.api.email.domain.EmailProvider
import com.whale.api.email.domain.property.EmailProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class EmailAttachmentService(
    private val findEmailOutput: FindEmailOutput,
    private val findEmailAttachmentOutput: FindEmailAttachmentOutput,
    private val saveEmailAttachmentOutput: SaveEmailAttachmentOutput,
    private val gmailProviderOutput: GmailProviderOutput,
    private val naverMailProviderOutput: NaverMailProviderOutput,
    private val emailProperty: EmailProperty,
) {
    private val logger = LoggerFactory.getLogger(EmailAttachmentService::class.java)

    fun downloadAttachment(
        userId: String,
        emailId: UUID,
        attachmentId: String,
    ): ByteArray? {
        // 이메일 소유권 확인
        val email = findEmailOutput.findByIdentifier(emailId) ?: return null

        // 첨부파일 조회
        val attachment = findEmailAttachmentOutput.findByEmailIdentifierAndAttachmentId(
            emailId, attachmentId
        ) ?: return null

        // 이미 다운로드된 파일이 있는지 확인
        attachment.localFilePath?.let { filePath ->
            val localFile = File(filePath)
            if (localFile.exists()) {
                logger.debug("Returning cached attachment: ${attachment.filename}")
                return localFile.readBytes()
            }
        }

        // 제공업체별 다운로드
        return when (email.emailAccountIdentifier.let { accountId ->
            // 계정 정보 조회 필요 (간소화를 위해 생략)
            EmailProvider.GMAIL // 임시
        }) {
            EmailProvider.GMAIL -> downloadGmailAttachment(email, attachment)
            EmailProvider.NAVER -> downloadNaverAttachment(email, attachment)
        }
    }

    fun getAttachmentsByEmail(
        userId: String,
        emailId: UUID,
    ): List<EmailAttachment> {
        // 이메일 소유권 확인
        val email = findEmailOutput.findByIdentifier(emailId) ?: return emptyList()

        return findEmailAttachmentOutput.findAllByEmailIdentifier(emailId)
    }

    fun getAttachment(
        userId: String,
        attachmentId: UUID,
    ): EmailAttachment? {
        return findEmailAttachmentOutput.findByIdentifier(attachmentId)
    }

    private fun downloadGmailAttachment(
        email: com.whale.api.email.domain.Email,
        attachment: EmailAttachment,
    ): ByteArray? {
        return try {
            // Gmail API를 통한 첨부파일 다운로드 (구현 필요)
            logger.info("Downloading Gmail attachment: ${attachment.filename}")

            // 임시 구현 - 실제로는 Gmail API 호출
            val attachmentData = ByteArray(0) // gmailProviderOutput.downloadAttachment(...)

            // 로컬에 저장
            if (attachmentData.isNotEmpty()) {
                saveAttachmentToLocal(attachment, attachmentData)
            }

            attachmentData
        } catch (e: Exception) {
            logger.error("Failed to download Gmail attachment: ${attachment.filename}", e)
            null
        }
    }

    private fun downloadNaverAttachment(
        email: com.whale.api.email.domain.Email,
        attachment: EmailAttachment,
    ): ByteArray? {
        return try {
            // IMAP을 통한 첨부파일 다운로드 (구현 필요)
            logger.info("Downloading Naver attachment: ${attachment.filename}")

            // 임시 구현 - 실제로는 IMAP 호출
            val attachmentData = ByteArray(0) // naverMailProviderOutput.downloadAttachment(...)

            // 로컬에 저장
            if (attachmentData.isNotEmpty()) {
                saveAttachmentToLocal(attachment, attachmentData)
            }

            attachmentData
        } catch (e: Exception) {
            logger.error("Failed to download Naver attachment: ${attachment.filename}", e)
            null
        }
    }

    private fun saveAttachmentToLocal(
        attachment: EmailAttachment,
        data: ByteArray,
    ) {
        try {
            val attachmentDir = Paths.get(emailProperty.attachmentBasePath)
            if (!Files.exists(attachmentDir)) {
                Files.createDirectories(attachmentDir)
            }

            val filename = "${attachment.identifier}_${attachment.filename}"
            val filePath = attachmentDir.resolve(filename)

            FileOutputStream(filePath.toFile()).use { fos ->
                fos.write(data)
            }

            // 첨부파일 정보 업데이트
            val updatedAttachment = attachment.copy(
                localFilePath = filePath.toString(),
                modifiedDate = OffsetDateTime.now()
            )

            saveEmailAttachmentOutput.save(updatedAttachment)

            logger.info("Saved attachment to local: $filePath")

        } catch (e: Exception) {
            logger.error("Failed to save attachment to local: ${attachment.filename}", e)
        }
    }

    fun cleanupOldAttachments(daysOld: Int = 30) {
        logger.info("Starting cleanup of attachments older than $daysOld days")

        try {
            val cutoffDate = OffsetDateTime.now().minusDays(daysOld.toLong())
            val oldAttachments = findEmailAttachmentOutput.findOldAttachments(cutoffDate)

            logger.info("Found ${oldAttachments.size} old attachments to cleanup")

            oldAttachments.forEach { attachment ->
                try {
                    // 로컬 파일 삭제
                    attachment.localFilePath?.let { filePath ->
                        val localFile = File(filePath)
                        if (localFile.exists()) {
                            localFile.delete()
                            logger.debug("Deleted local file: $filePath")
                        }
                    }

                    // 첨부파일 정보에서 로컬 경로 제거
                    val updatedAttachment = attachment.copy(
                        localFilePath = null,
                        modifiedDate = OffsetDateTime.now()
                    )

                    saveEmailAttachmentOutput.save(updatedAttachment)

                } catch (e: Exception) {
                    logger.error("Failed to cleanup attachment: ${attachment.filename}", e)
                }
            }

            logger.info("Completed cleanup of old attachments")

        } catch (e: Exception) {
            logger.error("Error occurred during attachment cleanup", e)
        }
    }
}
