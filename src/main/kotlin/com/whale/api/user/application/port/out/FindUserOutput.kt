package com.whale.api.user.application.port.out

import com.whale.api.user.domain.User

interface FindUserOutput {
    fun findByUsername(username: String): User?
}
