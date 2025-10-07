package com.whale.api.archive.domain

import java.time.OffsetDateTime
import java.util.UUID

class ArchiveTag(
    val identifier: UUID,
    val name: String,
    val type: String,
    val createdDate: OffsetDateTime,
) {
    companion object {
        fun create(name: String, type: String = "user"): ArchiveTag {
            return ArchiveTag(
                identifier = UUID.randomUUID(),
                name = name,
                type = type,
                createdDate = OffsetDateTime.now(),
            )
        }
    }
}
