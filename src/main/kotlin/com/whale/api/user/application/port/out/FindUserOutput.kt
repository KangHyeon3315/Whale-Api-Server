package com.whale.api.user.application.port.out

import com.whale.api.user.domain.User
import java.util.UUID

interface FindUserOutput {
    fun findByUsername(username: String): User?
    fun findByIdentifier(identifier: UUID): User?
}
