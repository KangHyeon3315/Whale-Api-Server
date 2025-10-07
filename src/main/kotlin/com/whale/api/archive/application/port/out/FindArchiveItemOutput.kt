package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveItem
import java.util.UUID

interface FindArchiveItemOutput {
    fun findArchiveItemById(identifier: UUID): ArchiveItem?
    fun findByArchiveIdentifier(archiveIdentifier: UUID): List<ArchiveItem>
    fun countByArchiveIdentifier(archiveIdentifier: UUID): Int
}
