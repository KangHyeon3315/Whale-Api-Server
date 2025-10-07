package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveItem
import java.time.OffsetDateTime
import java.util.UUID

interface FindArchiveItemOutput {
    fun findArchiveItemById(identifier: UUID): ArchiveItem?
    fun findByArchiveIdentifier(archiveIdentifier: UUID): List<ArchiveItem>
    fun findByArchiveIdentifierWithFilters(archiveIdentifier: UUID, fileName: String?, tags: List<String>?): List<ArchiveItem>
    fun findByArchiveIdentifierWithFiltersAndPagination(
        archiveIdentifier: UUID,
        fileName: String?,
        tags: List<String>?,
        cursor: OffsetDateTime?,
        limit: Int
    ): List<ArchiveItem>
    fun countByArchiveIdentifier(archiveIdentifier: UUID): Int
    fun countByArchiveIdentifierWithFilters(archiveIdentifier: UUID, fileName: String?, tags: List<String>?): Int
}
