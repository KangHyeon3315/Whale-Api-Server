package com.whale.api.global.jwt.enums

import java.time.Duration
import java.time.temporal.TemporalAmount

enum class TokenType(val validDuration: TemporalAmount) {
    ACCESS(Duration.ofHours(1)),
    REFRESH(Duration.ofDays(7)),
}
