package com.whale.api.repository.file

import com.whale.api.model.file.FileHashEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FileHashRepository : JpaRepository<FileHashEntity, UUID> {
    fun findByFileIdentifier(fileIdentifier: UUID): List<FileHashEntity>
    
    @Modifying
    @Query("DELETE FROM FileHashEntity fh WHERE fh.file.identifier = :fileIdentifier")
    fun deleteByFileIdentifier(@Param("fileIdentifier") fileIdentifier: UUID)
    
    fun findByHash(hash: String): List<FileHashEntity>
}
