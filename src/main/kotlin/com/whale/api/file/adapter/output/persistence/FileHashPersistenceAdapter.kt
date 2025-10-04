package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.FileHashEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.FileHashRepository
import com.whale.api.file.application.port.out.FindFileHashOutput
import com.whale.api.file.application.port.out.SaveFileHashOutput
import com.whale.api.file.domain.FileHash
import org.springframework.stereotype.Repository

@Repository
class FileHashPersistenceAdapter(
    private val fileHashRepository: FileHashRepository,
) : FindFileHashOutput,
    SaveFileHashOutput {
    override fun save(fileHash: FileHash): FileHash {
        return fileHashRepository.save(fileHash.toEntity()).toDomain()
    }

    override fun existByHash(hash: String): Boolean {
        return fileHashRepository.existsByHash(hash)
    }
}
