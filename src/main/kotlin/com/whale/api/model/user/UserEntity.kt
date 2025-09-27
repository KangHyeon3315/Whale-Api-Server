package com.whale.api.model.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "user")
data class UserEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "username", nullable = false)
    val username: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime,
    @Column(name = "modified_at", nullable = false)
    var modifiedAt: OffsetDateTime,
) {
    fun validatePassword(password: String): Boolean {
        return this.password == hashPassword(password)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
