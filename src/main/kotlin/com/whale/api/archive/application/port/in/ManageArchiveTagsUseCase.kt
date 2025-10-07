package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.ArchiveTag
import java.util.UUID

interface ManageArchiveTagsUseCase {
    fun addTagsToArchiveItem(itemIdentifier: UUID, tagNames: List<String>): List<ArchiveTag>
    fun removeTagsFromArchiveItem(itemIdentifier: UUID, tagNames: List<String>)
    fun updateArchiveItemTags(itemIdentifier: UUID, tagNames: List<String>): List<ArchiveTag>
}
