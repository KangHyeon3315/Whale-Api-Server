package com.whale.api.file.adapter.output.persistence.repository

import com.whale.api.file.adapter.output.persistence.entity.FileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface FileRepository : JpaRepository<FileEntity, UUID> {
    @Query("SELECT DISTINCT f.type FROM FileEntity f")
    fun findAllDistinctTypes(): List<String>
}
