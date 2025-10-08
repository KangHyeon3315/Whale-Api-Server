package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.ArchiveItem
import com.whale.api.archive.domain.ArchiveMetadata
import java.time.OffsetDateTime
import java.util.UUID

data class ArchiveItemPage(
    val items: List<ArchiveItem>,
    val hasNext: Boolean,
    val totalCount: Int,
)

interface GetArchiveItemsUseCase {
    fun getArchiveItems(archiveIdentifier: UUID): List<ArchiveItem>

    fun getArchiveItems(
        archiveIdentifier: UUID,
        fileName: String?,
        tags: List<String>?,
    ): List<ArchiveItem>

    fun getArchiveItems(
        archiveIdentifier: UUID,
        fileName: String?,
        tags: List<String>?,
        cursor: OffsetDateTime?,
        limit: Int,
    ): ArchiveItemPage

    fun getArchiveItem(itemIdentifier: UUID): ArchiveItem

    fun getArchiveItemMetadata(itemIdentifier: UUID): List<ArchiveMetadata>
}
