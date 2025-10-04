package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.FileHashEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.FileTagEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.FileTagRepository
import com.whale.api.file.application.port.out.SaveFileTagOutput
import com.whale.api.file.domain.FileTag
import org.springframework.stereotype.Repository

@Repository
class FileTagPersistenceAdapter(
    private val fileTagRepository: FileTagRepository,
) : SaveFileTagOutput {
    override fun saveAll(tags: List<FileTag>): List<FileTag> {
        return fileTagRepository.saveAll(tags.map { it.toEntity() }).map { it.toDomain() }
    }
}
