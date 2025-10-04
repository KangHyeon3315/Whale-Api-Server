package com.whale.api.user.application.port.`in`

import com.whale.api.global.jwt.model.Token
import com.whale.api.user.domain.dto.LoginResultDto

interface LoginUserUseCase {
    fun login(
        username: String,
        password: String,
    ): LoginResultDto

    fun refresh(refreshToken: String): Token
}
