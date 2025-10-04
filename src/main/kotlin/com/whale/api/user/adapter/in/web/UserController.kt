package com.whale.api.user.adapter.`in`.web

import com.tradepilot.userservice.user.adapter.input.web.response.RefreshResponse
import com.whale.api.global.annotation.WebController
import com.whale.api.global.constants.AuthConstants
import com.whale.api.user.adapter.`in`.web.request.LoginRequest
import com.whale.api.user.adapter.`in`.web.response.LoginResponse
import com.whale.api.user.application.port.`in`.LoginUserUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@WebController
class UserController(
    private val loginUserUseCase: LoginUserUseCase,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        return loginUserUseCase.login(request.username, request.password)
            .let { LoginResponse.Companion.of(it) }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("Authorization") refreshToken: String,
    ): ResponseEntity<RefreshResponse> {
        return loginUserUseCase.refresh(refreshToken.removePrefix(AuthConstants.BEARER_WITH_SPACE))
            .let { RefreshResponse.Companion.of(it) }
            .let { ResponseEntity.ok(it) }
    }
}
