package com.whale.api.file.adapter.output.persistence

import com.whale.api.file.adapter.output.persistence.entity.FileEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.entity.SaveTaskEntity.Companion.toEntity
import com.whale.api.file.adapter.output.persistence.repository.SaveTaskRepository
import com.whale.api.file.application.port.out.FindSaveTaskOutput
import com.whale.api.file.application.port.out.SaveSaveTaskOutput
import com.whale.api.file.domain.SaveTask
import com.whale.api.file.domain.enums.SaveTaskStatus
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class SaveTaskPersistenceAdapter(
    private val saveTaskRepository: SaveTaskRepository,
) : FindSaveTaskOutput,
    SaveSaveTaskOutput {
    override fun save(task: SaveTask): SaveTask {
        return saveTaskRepository.save(task.toEntity()).toDomain()
    }

    override fun findAllByStatus(status: SaveTaskStatus): List<SaveTask> {
        return saveTaskRepository.findAllByStatus(status.name).map { it.toDomain() }
    }

    override fun findByIdForUpdate(identifier: UUID): SaveTask? {
        return saveTaskRepository.findByIdForUpdate(identifier)?.toDomain()
    }
}
