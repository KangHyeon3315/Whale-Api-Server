package com.whale.api.user.adapter.out.persistence

import com.whale.api.user.adapter.out.persistence.UserEntity.Companion.toEntity
import com.whale.api.user.application.port.out.FindUserOutput
import com.whale.api.user.application.port.out.SaveUserOutput
import com.whale.api.user.domain.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserPersistenceAdapter(
    private val userRepository: UserRepository,
) : FindUserOutput,
    SaveUserOutput {
    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)?.toDomain()
    }

    override fun findByIdentifier(identifier: UUID): User? {
        return userRepository.findById(identifier).orElse(null)?.toDomain()
    }

    override fun save(user: User): User {
        return userRepository.save(user.toEntity()).toDomain()
    }
}
