package com.whale.api.model.user.dto

import com.whale.api.global.jwt.model.Token
import com.whale.api.model.user.UserEntity

data class LoginResultDto(
    val user: UserEntity,
    val accessToken: Token,
    val refreshToken: Token,
)
