package com.whale.api.archive.adapter.output.persistence.repository

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ArchiveTagRepository : JpaRepository<ArchiveTagEntity, UUID> {
    fun findByName(name: String): ArchiveTagEntity?
    fun findByNameAndType(name: String, type: String): ArchiveTagEntity?
    fun findAllByNameInAndTypeIn(names: List<String>, types: List<String>): List<ArchiveTagEntity>
}
