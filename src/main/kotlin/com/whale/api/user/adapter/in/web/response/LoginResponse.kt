package com.whale.api.user.adapter.`in`.web.response

import com.whale.api.global.jwt.enums.AuthRole
import com.whale.api.user.domain.dto.LoginResultDto
import java.time.OffsetDateTime
import java.util.UUID

data class LoginResponse(
    val identifier: UUID,
    val username: String,
    val accessToken: String,
    val accessTokenExpirationTime: OffsetDateTime,
    val refreshToken: String,
    val refreshTokenExpirationTime: OffsetDateTime,
    val roles: List<AuthRole>,
) {
    companion object {
        fun of(loginResultDto: LoginResultDto): LoginResponse {
            return LoginResponse(
                identifier = loginResultDto.user.identifier,
                username = loginResultDto.user.username,
                accessToken = loginResultDto.accessToken.token,
                accessTokenExpirationTime = loginResultDto.accessToken.expirationDate,
                refreshToken = loginResultDto.refreshToken.token,
                refreshTokenExpirationTime = loginResultDto.refreshToken.expirationDate,
                roles = AuthRole.entries,
            )
        }
    }
}
