package com.whale.api.user.application

import com.whale.api.global.jwt.JwtCrypter
import com.whale.api.global.jwt.enums.AuthRole
import com.whale.api.global.jwt.enums.TokenType
import com.whale.api.global.jwt.exceptions.UnauthorizedException
import com.whale.api.global.jwt.model.Token
import com.whale.api.user.application.port.`in`.LoginUserUseCase
import com.whale.api.user.application.port.`in`.UpdateUserTokenUseCase
import com.whale.api.user.application.port.out.FindUserOutput
import com.whale.api.user.application.port.out.SaveUserOutput
import com.whale.api.user.domain.User
import com.whale.api.user.domain.dto.LoginResultDto
import com.whale.api.user.domain.exception.InvalidAccountException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class UserService(
    private val crypter: JwtCrypter,
    private val findUserOutput: FindUserOutput,
    private val saveUserOutput: SaveUserOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : LoginUserUseCase,
    UpdateUserTokenUseCase {
    private val logger = KotlinLogging.logger {}

    override fun login(
        username: String,
        password: String,
    ): LoginResultDto {
        logger.info("Login request. username: $username")

        val (user, validationResult) =
            writeTransactionTemplate.execute {
                val user = findUserOutput.findByUsername(username)

                if (user == null) {
                    logger.info("Invalid account: $username")
                    throw InvalidAccountException()
                }

                val validationResult = validateUser(user, password)

                user to validationResult
            } ?: throw InvalidAccountException()

        if (!validationResult) {
            throw InvalidAccountException()
        }

        val accessToken =
            crypter.encrypt(
                userIdentifier = user.identifier,
                type = TokenType.ACCESS,
                roles = AuthRole.entries,
            )
        val refreshToken =
            crypter.encrypt(
                userIdentifier = user.identifier,
                type = TokenType.REFRESH,
                roles = AuthRole.entries,
            )

        return LoginResultDto(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun validateUser(
        user: User,
        password: String,
    ): Boolean {
        if (!user.validatePassword(password)) {
            logger.info("Invalid password. account: ${user.username}, password: $password")
            return false
        }

        return true
    }

    override fun refresh(refreshToken: String): Token {
        val token = crypter.decrypt(refreshToken)
        if (!token.isValid || token.type != TokenType.REFRESH) {
            throw UnauthorizedException()
        }

        return crypter.encrypt(
            userIdentifier = token.userIdentifier,
            type = TokenType.ACCESS,
            roles = token.roles,
        )
    }

    override fun updateToken(
        userIdentifier: UUID,
        token: String,
    ) {
        logger.info("Update token request. userIdentifier: $userIdentifier")

        writeTransactionTemplate.execute {
            val user =
                findUserOutput.findByIdentifier(userIdentifier)
                    ?: throw InvalidAccountException()

            user.updateToken(token)
            saveUserOutput.save(user)
        }
    }
}
