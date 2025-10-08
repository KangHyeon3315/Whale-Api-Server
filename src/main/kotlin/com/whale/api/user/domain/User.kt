package com.whale.api.user.domain

import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID

class User(
    val identifier: UUID,
    val username: String,
    val password: String,
    var token: String?,
    val createdAt: OffsetDateTime,
    val modifiedAt: OffsetDateTime,
) {
    fun updateToken(token: String) {
        this.token = token
    }

    fun validatePassword(password: String): Boolean {
        return this.password == hashPassword(password)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
