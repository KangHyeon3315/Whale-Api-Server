package com.whale.api.archive.adapter.input.web.response

import com.whale.api.archive.domain.Archive
import java.time.OffsetDateTime
import java.util.UUID

data class ArchiveResponse(
    val identifier: UUID,
    val name: String,
    val description: String?,
    val totalItems: Int,
    val processedItems: Int,
    val failedItems: Int,
    val progressPercentage: Double,
    val isCompleted: Boolean,
    val createdDate: OffsetDateTime,
    val modifiedDate: OffsetDateTime,
    val completedDate: OffsetDateTime?,
) {
    companion object {
        fun from(archive: Archive): ArchiveResponse {
            return ArchiveResponse(
                identifier = archive.identifier,
                name = archive.name,
                description = archive.description,
                totalItems = archive.totalItems,
                processedItems = archive.processedItems,
                failedItems = archive.failedItems,
                progressPercentage = archive.getProgressPercentage(),
                isCompleted = archive.isCompleted(),
                createdDate = archive.createdDate,
                modifiedDate = archive.modifiedDate,
                completedDate = archive.completedDate,
            )
        }
    }
}
