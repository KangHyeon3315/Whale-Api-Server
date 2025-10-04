package com.whale.api.user.adapter.out.persistence

import com.whale.api.user.application.port.out.FindUserOutput
import com.whale.api.user.application.port.out.SaveUserOutput
import com.whale.api.user.domain.User
import org.springframework.stereotype.Repository

@Repository
class UserPersistenceAdapter(
    private val userRepository: UserRepository,
) : FindUserOutput,
    SaveUserOutput {
    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)?.toDomain()
    }
}
