package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.ArchiveItem
import com.whale.api.archive.domain.ArchiveMetadata
import java.util.UUID

interface GetArchiveItemsUseCase {
    fun getArchiveItems(archiveIdentifier: UUID): List<ArchiveItem>
    fun getArchiveItem(itemIdentifier: UUID): ArchiveItem
    fun getArchiveItemMetadata(itemIdentifier: UUID): List<ArchiveMetadata>
}
