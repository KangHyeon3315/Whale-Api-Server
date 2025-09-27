package com.whale.api.global.exception

import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

data class ErrorResponse(
    val timestamp: OffsetDateTime,
    val path: String,
    val status: Int,
    val message: String
) {
    constructor(status: Int, path: String, message: String) : this(
        timestamp = OffsetDateTime.now(),
        path = path,
        status = status,
        message = message
    )
}
