package com.whale.api.user.application.port.out

import com.whale.api.user.domain.User

interface SaveUserOutput {
    fun save(user: User): User
}
