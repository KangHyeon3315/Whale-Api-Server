package com.whale.api.global.jwt.model

import java.time.OffsetDateTime

data class Token(
    val token: String,
    val expirationDate: OffsetDateTime,
) {
}
