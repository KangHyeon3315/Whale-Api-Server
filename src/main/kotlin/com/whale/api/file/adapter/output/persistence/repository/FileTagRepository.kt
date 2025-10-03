package com.whale.api.file.adapter.output.persistence.repository

import com.whale.api.file.adapter.output.persistence.entity.FileTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FileTagRepository : JpaRepository<FileTagEntity, UUID>
