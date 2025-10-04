package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.repository.UnsortedFileRepository
import com.whale.api.file.application.port.out.DeleteUnsortedFileOutput
import org.springframework.stereotype.Repository

@Repository
class UnsortedFilePersistenceAdapter(
    private val unsortedFileRepository: UnsortedFileRepository,
) : DeleteUnsortedFileOutput {
    override fun deleteByPath(path: String) {
        unsortedFileRepository.deleteByPath(path)
    }
}
