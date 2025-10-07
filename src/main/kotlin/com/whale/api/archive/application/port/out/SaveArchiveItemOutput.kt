package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveItem

interface SaveArchiveItemOutput {
    fun save(archiveItem: ArchiveItem): ArchiveItem
}
