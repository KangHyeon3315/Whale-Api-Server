package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveItemTag
import java.util.UUID

interface SaveArchiveItemTagOutput {
    fun save(itemTag: ArchiveItemTag): ArchiveItemTag
    fun saveAllItemTags(itemTags: List<ArchiveItemTag>): List<ArchiveItemTag>
    fun deleteByArchiveItemIdentifier(archiveItemIdentifier: UUID)
}
