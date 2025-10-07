package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveMetadata

interface SaveArchiveMetadataOutput {
    fun save(archiveMetadata: ArchiveMetadata): ArchiveMetadata
    fun saveAll(archiveMetadataList: List<ArchiveMetadata>): List<ArchiveMetadata>
}
