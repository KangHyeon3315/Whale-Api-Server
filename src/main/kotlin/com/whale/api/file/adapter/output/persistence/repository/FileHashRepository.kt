package com.whale.api.file.adapter.output.persistence.repository

import com.whale.api.file.adapter.output.persistence.entity.FileHashEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FileHashRepository : JpaRepository<FileHashEntity, UUID> {
    fun existsByHash(hash: String): Boolean
}
