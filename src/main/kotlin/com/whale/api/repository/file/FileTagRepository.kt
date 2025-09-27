package com.whale.api.repository.file

import com.whale.api.model.file.FileTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FileTagRepository : JpaRepository<FileTagEntity, UUID> {
    fun findByFileIdentifier(fileIdentifier: UUID): List<FileTagEntity>
    
    @Modifying
    @Query("DELETE FROM FileTagEntity ft WHERE ft.file.identifier = :fileIdentifier")
    fun deleteByFileIdentifier(@Param("fileIdentifier") fileIdentifier: UUID)
    
    @Query("""
        SELECT ft FROM FileTagEntity ft
        JOIN FETCH ft.tag
        WHERE ft.file.identifier IN :fileIdentifiers
    """)
    fun findByFileIdentifiersWithTag(@Param("fileIdentifiers") fileIdentifiers: List<UUID>): List<FileTagEntity>
}
