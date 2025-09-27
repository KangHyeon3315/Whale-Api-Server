package com.whale.api.global.security

import com.whale.api.global.jwt.enums.AuthRole
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID

class JwtAuthenticationToken : AbstractAuthenticationToken {
    private val userIdentifier: UUID
    private val token: String

    constructor(token: String) : super(null) {
        this.token = token
        this.userIdentifier = UUID.randomUUID() // 임시값, 실제로는 토큰에서 파싱
        isAuthenticated = false
    }

    constructor(
        userIdentifier: UUID,
        roles: List<AuthRole>,
        token: String,
    ) : super(roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }) {
        this.userIdentifier = userIdentifier
        this.token = token
        isAuthenticated = true
    }

    override fun getCredentials(): Any = token

    override fun getPrincipal(): Any = userIdentifier

    fun getUserIdentifier(): UUID = userIdentifier

    fun getToken(): String = token
}
