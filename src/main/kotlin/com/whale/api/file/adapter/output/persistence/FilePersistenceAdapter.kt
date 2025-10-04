package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.FileRepository
import com.whale.api.file.application.port.out.FindFileTypesOutput
import com.whale.api.file.application.port.out.SaveFileOutput
import com.whale.api.file.domain.File
import org.springframework.stereotype.Repository

@Repository
class FilePersistenceAdapter(
    private val fileRepository: FileRepository,
) : SaveFileOutput,
    FindFileTypesOutput {
    override fun save(file: File): File {
        return fileRepository.save(file.toEntity()).toDomain()
    }

    override fun findAllDistinctTypes(): List<String> {
        return fileRepository.findAllDistinctTypes()
    }
}
