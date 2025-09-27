package com.whale.api.controller.file.request

import java.util.UUID

data class UpdateFileGroupRequest(
    val name: String? = null,
    val thumbnail: String? = null,
    val tags: List<UUID>? = null
)
