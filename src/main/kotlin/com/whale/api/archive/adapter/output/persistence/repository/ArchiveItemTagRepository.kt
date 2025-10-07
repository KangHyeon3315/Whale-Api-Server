package com.whale.api.archive.adapter.output.persistence.repository

import com.whale.api.archive.adapter.output.persistence.entity.ArchiveItemTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ArchiveItemTagRepository : JpaRepository<ArchiveItemTagEntity, UUID> {
    fun findByArchiveItemIdentifier(archiveItemIdentifier: UUID): List<ArchiveItemTagEntity>
    
    @Query("SELECT ait.tag FROM ArchiveItemTagEntity ait WHERE ait.archiveItemIdentifier = :itemId")
    fun findTagsByArchiveItemIdentifier(@Param("itemId") archiveItemIdentifier: UUID): List<com.whale.api.archive.adapter.output.persistence.entity.ArchiveTagEntity>
    
    @Query("SELECT ait.archiveItemIdentifier FROM ArchiveItemTagEntity ait WHERE ait.tag.identifier = :tagId")
    fun findArchiveItemsByTagIdentifier(@Param("tagId") tagIdentifier: UUID): List<UUID>
    
    @Query("SELECT ait.archiveItemIdentifier FROM ArchiveItemTagEntity ait WHERE ait.tag.name = :tagName")
    fun findArchiveItemsByTagName(@Param("tagName") tagName: String): List<UUID>
    
    fun deleteByArchiveItemIdentifier(archiveItemIdentifier: UUID)
}
