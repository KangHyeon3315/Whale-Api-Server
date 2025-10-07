package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveTag

interface SaveArchiveTagOutput {
    fun save(tag: ArchiveTag): ArchiveTag
    fun saveAllTags(tags: List<ArchiveTag>): List<ArchiveTag>
}
