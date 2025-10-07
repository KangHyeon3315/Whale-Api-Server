package com.whale.api.archive.adapter.output.persistence

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveItemTagEntity.Companion.toEntity
import com.whale.api.archive.adapter.output.persistence.entity.ArchiveTagEntity.Companion.toEntity
import com.whale.api.archive.adapter.output.persistence.repository.ArchiveItemTagRepository
import com.whale.api.archive.adapter.output.persistence.repository.ArchiveTagRepository
import com.whale.api.archive.application.port.out.FindArchiveItemTagOutput
import com.whale.api.archive.application.port.out.FindArchiveTagOutput
import com.whale.api.archive.application.port.out.SaveArchiveItemTagOutput
import com.whale.api.archive.application.port.out.SaveArchiveTagOutput
import com.whale.api.archive.domain.ArchiveItemTag
import com.whale.api.archive.domain.ArchiveTag
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ArchiveTagPersistenceAdapter(
    private val archiveTagRepository: ArchiveTagRepository,
    private val archiveItemTagRepository: ArchiveItemTagRepository,
) : SaveArchiveTagOutput,
    FindArchiveTagOutput,
    SaveArchiveItemTagOutput,
    FindArchiveItemTagOutput {

    // ArchiveTag 관련
    override fun save(tag: ArchiveTag): ArchiveTag {
        return archiveTagRepository.save(tag.toEntity()).toDomain()
    }

    override fun saveAllTags(tags: List<ArchiveTag>): List<ArchiveTag> {
        val entities = tags.map { it.toEntity() }
        return archiveTagRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByName(name: String): ArchiveTag? {
        return archiveTagRepository.findByName(name)?.toDomain()
    }

    override fun findByNameAndType(name: String, type: String): ArchiveTag? {
        return archiveTagRepository.findByNameAndType(name, type)?.toDomain()
    }

    override fun findAllByNameInAndTypeIn(names: List<String>, types: List<String>): List<ArchiveTag> {
        return archiveTagRepository.findAllByNameInAndTypeIn(names, types).map { it.toDomain() }
    }

    override fun findAll(): List<ArchiveTag> {
        return archiveTagRepository.findAll().map { it.toDomain() }
    }

    override fun findById(identifier: UUID): ArchiveTag? {
        return archiveTagRepository.findById(identifier).orElse(null)?.toDomain()
    }

    // ArchiveItemTag 관련
    override fun save(itemTag: ArchiveItemTag): ArchiveItemTag {
        val tagEntity = archiveTagRepository.findById(itemTag.tagIdentifier).orElseThrow {
            IllegalArgumentException("Tag not found: ${itemTag.tagIdentifier}")
        }
        return archiveItemTagRepository.save(itemTag.toEntity(tagEntity)).toDomain()
    }

    override fun saveAllItemTags(itemTags: List<ArchiveItemTag>): List<ArchiveItemTag> {
        val tagIds = itemTags.map { it.tagIdentifier }.distinct()
        val tagEntities = archiveTagRepository.findAllById(tagIds).associateBy { it.identifier }

        val entities = itemTags.map { itemTag ->
            val tagEntity = tagEntities[itemTag.tagIdentifier]
                ?: throw IllegalArgumentException("Tag not found: ${itemTag.tagIdentifier}")
            itemTag.toEntity(tagEntity)
        }

        return archiveItemTagRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun deleteByArchiveItemIdentifier(archiveItemIdentifier: UUID) {
        archiveItemTagRepository.deleteByArchiveItemIdentifier(archiveItemIdentifier)
    }

    override fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveItemTag> {
        return archiveItemTagRepository.findByArchiveItemIdentifier(archiveItemIdentifier)
            .map { it.toDomain() }
    }

    override fun findTagsByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveTag> {
        return archiveItemTagRepository.findTagsByArchiveItemIdentifier(archiveItemIdentifier)
            .map { it.toDomain() }
    }

    override fun findArchiveItemsByTagIdentifier(tagIdentifier: UUID): List<UUID> {
        return archiveItemTagRepository.findArchiveItemsByTagIdentifier(tagIdentifier)
    }

    override fun findArchiveItemsByTagName(tagName: String): List<UUID> {
        return archiveItemTagRepository.findArchiveItemsByTagName(tagName)
    }
}
