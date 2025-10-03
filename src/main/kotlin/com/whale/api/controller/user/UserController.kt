package com.whale.api.controller.user

import com.tradepilot.userservice.user.adapter.input.web.response.RefreshResponse
import com.whale.api.controller.user.request.LoginRequest
import com.whale.api.controller.user.response.LoginResponse
import com.whale.api.global.annotation.WebController
import com.whale.api.global.constants.AuthConstants.BEARER_WITH_SPACE
import com.whale.api.service.user.LoginUserUseCase
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
            .let { LoginResponse.of(it) }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("Authorization") refreshToken: String,
    ): ResponseEntity<RefreshResponse> {
        return loginUserUseCase.refresh(refreshToken.removePrefix(BEARER_WITH_SPACE))
            .let { RefreshResponse.of(it) }
            .let { ResponseEntity.ok(it) }
    }
}
