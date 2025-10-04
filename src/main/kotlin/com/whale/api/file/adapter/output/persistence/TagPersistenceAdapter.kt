package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.TagEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.TagRepository
import com.whale.api.file.application.port.out.DeleteTagOutput
import com.whale.api.file.application.port.out.FindAllTagsOutput
import com.whale.api.file.application.port.out.FindTagOutput
import com.whale.api.file.application.port.out.SaveTagOutput
import com.whale.api.file.domain.Tag
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TagPersistenceAdapter(
    private val tagRepository: TagRepository,
) : FindTagOutput,
    SaveTagOutput,
    FindAllTagsOutput,
    DeleteTagOutput {
    override fun findAllByNameInAndTypeIn(
        names: List<String>,
        types: List<String>,
    ): List<Tag> {
        return tagRepository.findAllByNameInAndTypeIn(names, types).map { it.toDomain() }
    }

    override fun saveAll(tags: List<Tag>): List<Tag> {
        return tagRepository.saveAll(tags.map { it.toEntity() }).map { it.toDomain() }
    }

    override fun deleteByIdentifiers(identifiers: List<UUID>) {
        tagRepository.deleteAllById(identifiers)
    }

    override fun findAllTags(): List<Tag> {
        return tagRepository.findAll().map { it.toDomain() }
    }
}
