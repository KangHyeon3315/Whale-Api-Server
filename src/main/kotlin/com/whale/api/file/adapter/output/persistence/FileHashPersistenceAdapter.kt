package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.FileHashEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.FileHashRepository
import com.whale.api.file.application.port.out.DeleteFileHashOutput
import com.whale.api.file.application.port.out.FindFileHashOutput
import com.whale.api.file.application.port.out.SaveFileHashOutput
import com.whale.api.file.domain.FileHash
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class FileHashPersistenceAdapter(
    private val fileHashRepository: FileHashRepository,
) : FindFileHashOutput,
    SaveFileHashOutput,
    DeleteFileHashOutput {
    override fun save(fileHash: FileHash): FileHash {
        return fileHashRepository.save(fileHash.toEntity()).toDomain()
    }

    override fun deleteByFileIdentifier(fileIdentifier: UUID) {
        fileHashRepository.deleteByFileIdentifier(fileIdentifier)
    }

    override fun existByHash(hash: String): Boolean {
        return fileHashRepository.existsByHash(hash)
    }
}
