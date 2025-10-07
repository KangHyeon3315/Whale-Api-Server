package com.whale.api.archive.domain

import java.time.OffsetDateTime
import java.util.UUID

class ArchiveItemTag(
    val identifier: UUID,
    val archiveItemIdentifier: UUID,
    val tagIdentifier: UUID,
    val createdDate: OffsetDateTime,
) {
    companion object {
        fun create(archiveItemIdentifier: UUID, tagIdentifier: UUID): ArchiveItemTag {
            return ArchiveItemTag(
                identifier = UUID.randomUUID(),
                archiveItemIdentifier = archiveItemIdentifier,
                tagIdentifier = tagIdentifier,
                createdDate = OffsetDateTime.now(),
            )
        }
    }
}
