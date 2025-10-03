package com.whale.api.file.domain

import java.time.OffsetDateTime
import java.util.UUID

class FileGroup(
    val identifier: UUID,
    val name: String,
    val type: String,
    var thumbnail: String?,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime?
) {
}
