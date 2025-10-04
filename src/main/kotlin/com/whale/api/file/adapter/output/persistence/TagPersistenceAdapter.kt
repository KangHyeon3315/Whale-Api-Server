package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.TagEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.TagRepository
import com.whale.api.file.application.port.out.FindTagOutput
import com.whale.api.file.application.port.out.SaveTagOutput
import com.whale.api.file.domain.Tag
import org.springframework.stereotype.Repository

@Repository
class TagPersistenceAdapter(
    private val tagRepository: TagRepository,
) : FindTagOutput,
    SaveTagOutput {
    override fun findAllByNameInAndTypeIn(
        names: List<String>,
        types: List<String>,
    ): List<Tag> {
        return tagRepository.findAllByNameInAndTypeIn(names, types).map { it.toDomain() }
    }

    override fun saveAll(tags: List<Tag>): List<Tag> {
        return tagRepository.saveAll(tags.map { it.toEntity() }).map { it.toDomain() }
    }
}
