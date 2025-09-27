package com.whale.api.controller.file.request

import java.util.UUID

data class UpdateFileRequest(
    val name: String? = null,
    val thumbnail: String? = null,
    val sortOrder: Int? = null,
    val tags: List<UUID>? = null
)
