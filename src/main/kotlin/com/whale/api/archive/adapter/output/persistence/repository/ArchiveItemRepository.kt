package com.whale.api.archive.adapter.output.persistence.repository

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ArchiveItemRepository : JpaRepository<ArchiveItemEntity, UUID> {
    fun findByArchiveIdentifier(archiveIdentifier: UUID): List<ArchiveItemEntity>

    fun countByArchiveIdentifier(archiveIdentifier: UUID): Int
}
