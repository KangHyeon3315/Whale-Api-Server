package com.whale.api.user.application.port.`in`

import java.util.UUID

interface UpdateUserTokenUseCase {
    fun updateToken(
        userIdentifier: UUID,
        token: String,
    )
}
