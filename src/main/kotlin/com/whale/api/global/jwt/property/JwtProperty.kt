package com.whale.api.global.jwt.property

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtProperty(
    @Value("\${jwt.secret}")
    private val secretStr: String,
) {

    val secret: SecretKey = Keys.hmacShaKeyFor(secretStr.toByteArray())
}
