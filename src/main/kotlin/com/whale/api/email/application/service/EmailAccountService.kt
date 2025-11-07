package com.whale.api.email.application.service

import com.whale.api.email.application.port.`in`.GetEmailUseCase
import com.whale.api.email.application.port.`in`.RegisterEmailAccountUseCase
import com.whale.api.email.application.port.`in`.command.RegisterEmailAccountCommand
import com.whale.api.email.application.port.out.EncryptionOutput
import com.whale.api.email.application.port.out.FindEmailAccountOutput
import com.whale.api.email.application.port.out.FindEmailOutput
import com.whale.api.email.application.port.out.GmailProviderOutput
import com.whale.api.email.application.port.out.NaverMailProviderOutput
import com.whale.api.email.application.port.out.SaveEmailAccountOutput
import com.whale.api.email.domain.Email
import com.whale.api.email.domain.EmailAccount
import com.whale.api.email.domain.EmailProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class EmailAccountService(
    private val saveEmailAccountOutput: SaveEmailAccountOutput,
    private val findEmailAccountOutput: FindEmailAccountOutput,
    private val findEmailOutput: FindEmailOutput,
    private val gmailProviderOutput: GmailProviderOutput,
    private val naverMailProviderOutput: NaverMailProviderOutput,
    private val encryptionOutput: EncryptionOutput,
) : RegisterEmailAccountUseCase, GetEmailUseCase {
    override fun registerGmailAccount(command: RegisterEmailAccountCommand): EmailAccount {
        validateGmailCommand(command)

        // 기존 계정 중복 확인
        val existingAccount =
            findEmailAccountOutput.findByUserIdAndEmailAddress(
                command.userId,
                command.emailAddress,
            )
        if (existingAccount != null) {
            throw EmailAccountAlreadyExistsException("Email account already exists: ${command.emailAddress}")
        }

        // OAuth2 토큰 교환
        val tokenInfo =
            gmailProviderOutput.exchangeCodeForTokens(
                command.authorizationCode!!,
                command.emailAddress,
            )

        val emailAccount =
            EmailAccount(
                identifier = UUID.randomUUID(),
                userId = command.userId,
                emailAddress = command.emailAddress,
                provider = EmailProvider.GMAIL,
                displayName = command.displayName,
                accessToken = tokenInfo.accessToken,
                refreshToken = tokenInfo.refreshToken,
                tokenExpiry = OffsetDateTime.now().plusSeconds(tokenInfo.expiresInSeconds),
                encryptedPassword = null,
                isActive = true,
                syncEnabled = true,
                lastSyncDate = null,
                createdDate = OffsetDateTime.now(),
                modifiedDate = OffsetDateTime.now(),
            )

        return saveEmailAccountOutput.save(emailAccount)
    }

    override fun registerNaverAccount(command: RegisterEmailAccountCommand): EmailAccount {
        validateNaverCommand(command)

        // 기존 계정 중복 확인
        val existingAccount =
            findEmailAccountOutput.findByUserIdAndEmailAddress(
                command.userId,
                command.emailAddress,
            )
        if (existingAccount != null) {
            throw EmailAccountAlreadyExistsException("Email account already exists: ${command.emailAddress}")
        }

        // 연결 테스트
        val connectionTest =
            naverMailProviderOutput.testConnection(
                command.emailAddress,
                command.password!!,
            )
        if (!connectionTest) {
            throw InvalidEmailCredentialsException("Invalid email credentials for Naver Mail")
        }

        val emailAccount =
            EmailAccount(
                identifier = UUID.randomUUID(),
                userId = command.userId,
                emailAddress = command.emailAddress,
                provider = EmailProvider.NAVER,
                displayName = command.displayName,
                accessToken = null,
                refreshToken = null,
                tokenExpiry = null,
                encryptedPassword = encryptionOutput.encrypt(command.password),
                isActive = true,
                syncEnabled = true,
                lastSyncDate = null,
                createdDate = OffsetDateTime.now(),
                modifiedDate = OffsetDateTime.now(),
            )

        return saveEmailAccountOutput.save(emailAccount)
    }

    override fun getGmailAuthUrl(userId: String): String {
        return gmailProviderOutput.getAuthorizationUrl(userId)
    }

    override fun handleGmailOAuthCallback(
        userId: String,
        authorizationCode: String,
    ): EmailAccount {
        // 임시로 이메일 주소를 추출하는 로직이 필요
        // 실제로는 OAuth2 콜백에서 사용자 정보를 가져와야 함
        throw NotImplementedError("OAuth callback handling needs user email extraction")
    }

    override fun getEmailAccounts(userId: String): List<EmailAccount> {
        return findEmailAccountOutput.findAllByUserId(userId)
    }

    override fun getEmailAccount(
        userId: String,
        accountId: UUID,
    ): EmailAccount? {
        return findEmailAccountOutput.findByUserIdAndIdentifier(userId, accountId)
    }

    override fun getEmails(
        userId: String,
        accountId: UUID,
        folderName: String?,
        isRead: Boolean?,
        limit: Int,
        offset: Int,
    ): List<Email> {
        // 계정 소유권 확인
        val account =
            findEmailAccountOutput.findByUserIdAndIdentifier(userId, accountId)
                ?: throw EmailAccountNotFoundException("Email account not found: $accountId")

        return findEmailOutput.findByAccountIdentifier(
            accountIdentifier = accountId,
            folderName = folderName,
            isRead = isRead,
            limit = limit,
            offset = offset,
        )
    }

    override fun getEmail(
        userId: String,
        emailId: UUID,
    ): Email? {
        val email = findEmailOutput.findByIdentifier(emailId) ?: return null

        // 계정 소유권 확인
        val account =
            findEmailAccountOutput.findByUserIdAndIdentifier(userId, email.emailAccountIdentifier)
                ?: return null

        return email
    }

    override fun searchEmails(
        userId: String,
        accountId: UUID?,
        query: String,
        limit: Int,
        offset: Int,
    ): List<Email> {
        // 계정 소유권 확인 (특정 계정 검색인 경우)
        if (accountId != null) {
            val account =
                findEmailAccountOutput.findByUserIdAndIdentifier(userId, accountId)
                    ?: throw EmailAccountNotFoundException("Email account not found: $accountId")
        }

        return findEmailOutput.searchEmails(
            accountIdentifier = accountId,
            query = query,
            limit = limit,
            offset = offset,
        )
    }

    private fun validateGmailCommand(command: RegisterEmailAccountCommand) {
        if (command.provider != EmailProvider.GMAIL) {
            throw IllegalArgumentException("Invalid provider for Gmail registration")
        }
        if (command.authorizationCode.isNullOrBlank()) {
            throw IllegalArgumentException("Authorization code is required for Gmail registration")
        }
    }

    private fun validateNaverCommand(command: RegisterEmailAccountCommand) {
        if (command.provider != EmailProvider.NAVER) {
            throw IllegalArgumentException("Invalid provider for Naver registration")
        }
        if (command.password.isNullOrBlank()) {
            throw IllegalArgumentException("Password is required for Naver registration")
        }
    }
}

class EmailAccountAlreadyExistsException(message: String) : RuntimeException(message)

class InvalidEmailCredentialsException(message: String) : RuntimeException(message)

class EmailAccountNotFoundException(message: String) : RuntimeException(message)
