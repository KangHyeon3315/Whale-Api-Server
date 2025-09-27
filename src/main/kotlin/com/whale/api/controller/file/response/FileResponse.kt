package com.whale.api.controller.file.response

import java.time.OffsetDateTime
import java.util.UUID

data class FileResponse(
    val identifier: UUID,
    val fileGroupIdentifier: UUID?,
    val name: String,
    val type: String,
    val path: String,
    val thumbnail: String?,
    val sortOrder: Int?,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime?,
    val lastViewDate: OffsetDateTime?,
    val tags: List<TagResponse> = emptyList()
)

data class TagResponse(
    val identifier: UUID,
    val name: String,
    val type: String
)
