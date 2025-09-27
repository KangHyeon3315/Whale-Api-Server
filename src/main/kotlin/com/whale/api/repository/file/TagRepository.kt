package com.whale.api.repository.file

import com.whale.api.model.file.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TagRepository : JpaRepository<TagEntity, UUID> {
    fun findByName(name: String): TagEntity?
    fun findByType(type: String): List<TagEntity>

    @Query("SELECT t FROM TagEntity t WHERE (t.name, t.type) IN :nameTypePairs")
    fun findByNameAndTypeIn(@Param("nameTypePairs") nameTypePairs: List<Pair<String, String>>): List<TagEntity>
}
