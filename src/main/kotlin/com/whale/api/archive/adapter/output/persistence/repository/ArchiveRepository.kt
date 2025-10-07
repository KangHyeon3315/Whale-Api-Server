package com.whale.api.archive.adapter.output.persistence.repository

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ArchiveRepository : JpaRepository<ArchiveEntity, UUID>
