package com.whale.api.user.adapter.out.persistence

import com.whale.api.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
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
    fun toDomain(): User {
        return User(
            identifier = this.identifier,
            username = this.username,
            password = this.password,
            createdAt = this.createdAt,
            modifiedAt = this.modifiedAt,
        )
    }

    companion object {
        fun User.toEntity(): UserEntity {
            return UserEntity(
                identifier = this.identifier,
                username = this.username,
                password = this.password,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
            )
        }
    }
}
