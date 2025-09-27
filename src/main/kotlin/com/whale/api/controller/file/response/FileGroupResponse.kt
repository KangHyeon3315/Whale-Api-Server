package com.whale.api.controller.file.response

import java.time.OffsetDateTime
import java.util.UUID

data class FileGroupResponse(
    val identifier: UUID,
    val name: String,
    val type: String,
    val thumbnail: String?,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime?,
    val files: List<FileResponse> = emptyList(),
    val tags: List<TagResponse> = emptyList()
)
