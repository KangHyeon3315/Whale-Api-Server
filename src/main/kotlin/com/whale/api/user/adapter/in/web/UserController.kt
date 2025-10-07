package com.whale.api.user.adapter.`in`.web

import com.tradepilot.userservice.user.adapter.input.web.response.RefreshResponse
import com.whale.api.global.annotation.RequireAuth
import com.whale.api.global.annotation.WebController
import com.whale.api.global.constants.AuthConstants
import com.whale.api.user.adapter.`in`.web.request.LoginRequest
import com.whale.api.user.adapter.`in`.web.request.UpdateTokenRequest
import com.whale.api.user.adapter.`in`.web.response.LoginResponse
import com.whale.api.user.adapter.`in`.web.response.UpdateTokenResponse
import com.whale.api.user.application.port.`in`.LoginUserUseCase
import com.whale.api.user.application.port.`in`.UpdateUserTokenUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.util.UUID

@WebController
class UserController(
    private val loginUserUseCase: LoginUserUseCase,
    private val updateUserTokenUseCase: UpdateUserTokenUseCase,
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

    @RequireAuth
    @PutMapping("/token")
    fun updateToken(
        @AuthenticationPrincipal userIdentifier: UUID,
        @RequestBody request: UpdateTokenRequest,
    ): ResponseEntity<UpdateTokenResponse> {
        updateUserTokenUseCase.updateToken(userIdentifier, request.token)
        return ResponseEntity.ok(UpdateTokenResponse.success())
    }
}
