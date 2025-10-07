package com.whale.api.archive.adapter.output.persistence

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveEntity.Companion.toEntity
import com.whale.api.archive.adapter.output.persistence.entity.ArchiveItemEntity.Companion.toEntity
import com.whale.api.archive.adapter.output.persistence.entity.ArchiveMetadataEntity.Companion.toEntity
import com.whale.api.archive.adapter.output.persistence.repository.ArchiveItemRepository
import com.whale.api.archive.adapter.output.persistence.repository.ArchiveMetadataRepository
import com.whale.api.archive.adapter.output.persistence.repository.ArchiveRepository
import com.whale.api.archive.application.port.out.FindArchiveItemOutput
import com.whale.api.archive.application.port.out.FindArchiveMetadataOutput
import com.whale.api.archive.application.port.out.FindArchiveOutput
import com.whale.api.archive.application.port.out.SaveArchiveItemOutput
import com.whale.api.archive.application.port.out.SaveArchiveMetadataOutput
import com.whale.api.archive.application.port.out.SaveArchiveOutput
import com.whale.api.archive.domain.Archive
import com.whale.api.archive.domain.ArchiveItem
import com.whale.api.archive.domain.ArchiveMetadata
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ArchivePersistenceAdapter(
    private val archiveRepository: ArchiveRepository,
    private val archiveItemRepository: ArchiveItemRepository,
    private val archiveMetadataRepository: ArchiveMetadataRepository,
) : SaveArchiveOutput,
    FindArchiveOutput,
    SaveArchiveItemOutput,
    FindArchiveItemOutput,
    SaveArchiveMetadataOutput,
    FindArchiveMetadataOutput {

    override fun save(archive: Archive): Archive {
        return archiveRepository.save(archive.toEntity()).toDomain()
    }

    override fun findById(identifier: UUID): Archive? {
        return archiveRepository.findById(identifier).orElse(null)?.toDomain()
    }

    override fun findAll(): List<Archive> {
        return archiveRepository.findAll().map { it.toDomain() }
    }

    override fun save(archiveItem: ArchiveItem): ArchiveItem {
        return archiveItemRepository.save(archiveItem.toEntity()).toDomain()
    }

    override fun findById(identifier: UUID): ArchiveItem? {
        return archiveItemRepository.findById(identifier).orElse(null)?.toDomain()
    }

    override fun findByArchiveIdentifier(archiveIdentifier: UUID): List<ArchiveItem> {
        return archiveItemRepository.findByArchiveIdentifier(archiveIdentifier).map { it.toDomain() }
    }

    override fun countByArchiveIdentifier(archiveIdentifier: UUID): Int {
        return archiveItemRepository.countByArchiveIdentifier(archiveIdentifier)
    }

    override fun save(archiveMetadata: ArchiveMetadata): ArchiveMetadata {
        return archiveMetadataRepository.save(archiveMetadata.toEntity()).toDomain()
    }

    override fun saveAll(archiveMetadataList: List<ArchiveMetadata>): List<ArchiveMetadata> {
        val entities = archiveMetadataList.map { it.toEntity() }
        return archiveMetadataRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveMetadata> {
        return archiveMetadataRepository.findByArchiveItemIdentifier(archiveItemIdentifier).map { it.toDomain() }
    }
}
