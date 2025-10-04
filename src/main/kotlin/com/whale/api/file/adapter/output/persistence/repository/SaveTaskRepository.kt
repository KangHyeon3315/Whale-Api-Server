package com.whale.api.file.adapter.output.persistence.repository

import com.whale.api.file.adapter.output.persistence.entity.SaveTaskEntity
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SaveTaskRepository : JpaRepository<SaveTaskEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")])
    @Query("SELECT t FROM SaveTaskEntity t WHERE t.identifier = :identifier")
    fun findByIdForUpdate(
        @Param("identifier") identifier: UUID,
    ): SaveTaskEntity?

    fun findAllByStatus(status: String): List<SaveTaskEntity>
}
