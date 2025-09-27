package com.whale.api.repository.file

import com.whale.api.model.file.FileGroupTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FileGroupTagRepository : JpaRepository<FileGroupTagEntity, UUID> {
    fun findByFileGroupIdentifier(fileGroupIdentifier: UUID): List<FileGroupTagEntity>
    
    @Modifying
    @Query("DELETE FROM FileGroupTagEntity fgt WHERE fgt.fileGroup.identifier = :fileGroupIdentifier")
    fun deleteByFileGroupIdentifier(@Param("fileGroupIdentifier") fileGroupIdentifier: UUID)
    
    @Query("""
        SELECT fgt FROM FileGroupTagEntity fgt
        JOIN FETCH fgt.tag
        WHERE fgt.fileGroup.identifier IN :fileGroupIdentifiers
    """)
    fun findByFileGroupIdentifiersWithTag(@Param("fileGroupIdentifiers") fileGroupIdentifiers: List<UUID>): List<FileGroupTagEntity>
}
