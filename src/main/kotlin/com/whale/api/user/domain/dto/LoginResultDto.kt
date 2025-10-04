package com.whale.api.user.domain.dto

import com.whale.api.global.jwt.model.Token
import com.whale.api.user.domain.User

data class LoginResultDto(
    val user: User,
    val accessToken: Token,
    val refreshToken: Token,
)
