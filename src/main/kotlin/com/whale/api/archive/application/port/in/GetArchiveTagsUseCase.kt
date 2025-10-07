package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.ArchiveTag
import java.util.UUID

interface GetArchiveTagsUseCase {
    fun getAllTags(): List<ArchiveTag>
    fun getTagsByArchiveItem(itemIdentifier: UUID): List<ArchiveTag>
    fun getArchiveItemsByTag(tagName: String): List<UUID>
}
