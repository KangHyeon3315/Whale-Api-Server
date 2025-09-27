package com.whale.api.repository.file

import com.whale.api.model.file.FileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FileRepository : JpaRepository<FileEntity, UUID> {
    fun findByFileGroupIdentifier(fileGroupIdentifier: UUID): List<FileEntity>
    
    @Query("SELECT DISTINCT f.type FROM FileEntity f")
    fun findDistinctTypes(): List<String>
    
    @Query("""
        SELECT f FROM FileEntity f 
        WHERE f.fileGroup.identifier = :fileGroupIdentifier
        ORDER BY f.sortOrder ASC
    """)
    fun findByFileGroupIdentifierOrderBySortOrder(@Param("fileGroupIdentifier") fileGroupIdentifier: UUID): List<FileEntity>
}
