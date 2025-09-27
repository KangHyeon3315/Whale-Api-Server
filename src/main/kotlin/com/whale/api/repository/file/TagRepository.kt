package com.whale.api.repository.file

import com.whale.api.model.file.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TagRepository : JpaRepository<TagEntity, UUID> {
    fun findByName(name: String): TagEntity?
    fun findByType(type: String): List<TagEntity>
}
