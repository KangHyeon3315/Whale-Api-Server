package com.whale.api.controller.file.response

import java.util.UUID

data class SaveFileResponse(
    val message: String,
    val eventId: UUID?,
    val fileIdentifier: UUID,
    val status: String
)
