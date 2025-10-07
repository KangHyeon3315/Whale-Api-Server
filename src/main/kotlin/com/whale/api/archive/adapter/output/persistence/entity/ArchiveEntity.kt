package com.whale.api.archive.adapter.output.persistence.entity

import com.whale.api.archive.domain.Archive
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "archive")
data class ArchiveEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "description", nullable = true)
    val description: String?,
    @Column(name = "total_items", nullable = false)
    val totalItems: Int,
    @Column(name = "processed_items", nullable = false)
    val processedItems: Int,
    @Column(name = "failed_items", nullable = false)
    val failedItems: Int,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    val modifiedDate: OffsetDateTime,
    @Column(name = "completed_date", nullable = true)
    val completedDate: OffsetDateTime?,
) {
    fun toDomain(): Archive {
        return Archive(
            identifier = this.identifier,
            name = this.name,
            description = this.description,
            totalItems = this.totalItems,
            processedItems = this.processedItems,
            failedItems = this.failedItems,
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
            completedDate = this.completedDate,
        )
    }

    companion object {
        fun Archive.toEntity(): ArchiveEntity {
            return ArchiveEntity(
                identifier = this.identifier,
                name = this.name,
                description = this.description,
                totalItems = this.totalItems,
                processedItems = this.processedItems,
                failedItems = this.failedItems,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
                completedDate = this.completedDate,
            )
        }
    }
}
