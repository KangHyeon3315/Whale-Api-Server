package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveItemTag
import com.whale.api.archive.domain.ArchiveTag
import java.util.UUID

interface FindArchiveItemTagOutput {
    fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveItemTag>
    fun findTagsByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveTag>
    fun findArchiveItemsByTagIdentifier(tagIdentifier: UUID): List<UUID>
    fun findArchiveItemsByTagName(tagName: String): List<UUID>
}
