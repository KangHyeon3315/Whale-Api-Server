package com.tradepilot.userservice.user.adapter.input.web.response

import com.whale.api.global.jwt.model.Token

data class RefreshResponse(
    val accessToken: String,
    val accessTokenExpirationTime: String,
) {
    companion object {
        fun of(token: Token): RefreshResponse {
            return RefreshResponse(
                accessToken = token.token,
                accessTokenExpirationTime = token.expirationDate.toString(),
            )
        }
    }
}
