package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveMetadata
import java.util.UUID

interface FindArchiveMetadataOutput {
    fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveMetadata>
}
