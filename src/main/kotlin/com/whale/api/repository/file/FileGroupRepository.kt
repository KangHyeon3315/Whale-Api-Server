package com.whale.api.repository.file

import com.whale.api.model.file.FileGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface FileGroupRepository : JpaRepository<FileGroupEntity, UUID> {
    
    @Query("""
        SELECT fg FROM FileGroupEntity fg
        WHERE (:type IS NULL OR fg.type = :type)
        AND (:keyword IS NULL OR fg.name LIKE %:keyword%)
        ORDER BY 
            CASE WHEN :sort = 'name' AND :order = 'asc' THEN fg.name END ASC,
            CASE WHEN :sort = 'name' AND :order = 'desc' THEN fg.name END DESC,
            CASE WHEN :sort = 'created_date' AND :order = 'asc' THEN fg.createdDate END ASC,
            CASE WHEN :sort = 'created_date' AND :order = 'desc' THEN fg.createdDate END DESC
    """)
    fun searchFileGroups(
        @Param("type") type: String?,
        @Param("keyword") keyword: String?,
        @Param("sort") sort: String,
        @Param("order") order: String
    ): List<FileGroupEntity>
    
    @Query("""
        SELECT fg FROM FileGroupEntity fg
        JOIN FileGroupTagEntity fgt ON fg.identifier = fgt.fileGroup.identifier
        WHERE fgt.tag.identifier IN :tagIdentifiers
        GROUP BY fg.identifier
        HAVING COUNT(DISTINCT fgt.tag.identifier) = :tagCount
    """)
    fun findByTagIdentifiers(
        @Param("tagIdentifiers") tagIdentifiers: List<UUID>,
        @Param("tagCount") tagCount: Long
    ): List<FileGroupEntity>
}
