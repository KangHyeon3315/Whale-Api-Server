package com.whale.api.controller.file.request

import java.util.UUID

data class SaveFileGroupRequest(
    val name: String,
    val type: String,
    val path: String,
    val tags: List<UUID> = emptyList(),
    val thumbnail: String? = null
)
