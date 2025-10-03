package com.whale.api.file.domain

import java.time.OffsetDateTime
import java.util.UUID

class File(
    val identifier: UUID,
    val fileGroupIdentifier: UUID?,
    val name: String,
    val type: String,
    val path: String,
    var thumbnail: String?,
    var sortOrder: Int?,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime,
    var lastViewDate: OffsetDateTime?
) {
}
