package com.whale.api.repository.file

import com.whale.api.model.file.UnsortedFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UnsortedFileRepository : JpaRepository<UnsortedFileEntity, UUID> {
    fun findByPath(path: String): UnsortedFileEntity?
    fun existsByPath(path: String): Boolean
    fun deleteByPath(path: String): Int
}
