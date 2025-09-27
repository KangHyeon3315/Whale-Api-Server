package com.whale.api.service.user

import com.whale.api.global.jwt.model.Token
import com.whale.api.model.user.dto.LoginResultDto

interface LoginUserUseCase {
    fun login(
        username: String,
        password: String,
    ): LoginResultDto

    fun refresh(refreshToken: String): Token
}
