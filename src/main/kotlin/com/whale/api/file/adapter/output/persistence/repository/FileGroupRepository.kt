package com.whale.api.file.adapter.output.persistence.repository

import com.whale.api.file.adapter.output.persistence.entity.FileGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FileGroupRepository : JpaRepository<FileGroupEntity, UUID> {


}
