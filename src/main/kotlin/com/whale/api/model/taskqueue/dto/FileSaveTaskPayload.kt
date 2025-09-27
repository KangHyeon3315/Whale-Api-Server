package com.whale.api.model.taskqueue.dto

import java.util.UUID

data class FileSaveTaskPayload(
    val fileIdentifier: UUID,
    val name: String,
    val path: String,
    val type: String,
    val tags: List<TagDto>,
    val basePath: String
)

data class TagDto(
    val name: String,
    val type: String
)
