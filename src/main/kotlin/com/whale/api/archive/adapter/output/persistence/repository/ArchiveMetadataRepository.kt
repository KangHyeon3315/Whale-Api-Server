package com.whale.api.archive.adapter.output.persistence.repository

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ArchiveMetadataRepository : JpaRepository<ArchiveMetadataEntity, UUID> {
    fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveMetadataEntity>

    fun deleteByArchiveItemIdentifier(archiveItemIdentifier: UUID)
}
