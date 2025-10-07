package com.whale.api.archive.domain

import com.whale.api.archive.domain.enums.ArchiveStatus
import java.time.OffsetDateTime
import java.util.UUID

class Archive(
    val identifier: UUID,
    val name: String,
    val description: String?,
    var status: ArchiveStatus,
    var totalItems: Int,
    var processedItems: Int,
    var failedItems: Int,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime,
    var completedDate: OffsetDateTime?,
) {
    fun start() {
        status = ArchiveStatus.IN_PROGRESS
        modifiedDate = OffsetDateTime.now()
    }

    fun complete() {
        status = ArchiveStatus.COMPLETED
        modifiedDate = OffsetDateTime.now()
        completedDate = OffsetDateTime.now()
    }

    fun fail() {
        status = ArchiveStatus.FAILED
        modifiedDate = OffsetDateTime.now()
    }

    fun cancel() {
        status = ArchiveStatus.CANCELLED
        modifiedDate = OffsetDateTime.now()
    }

    fun incrementProcessedItems() {
        processedItems++
        modifiedDate = OffsetDateTime.now()
    }

    fun incrementFailedItems() {
        failedItems++
        modifiedDate = OffsetDateTime.now()
    }

    fun updateTotalItems(total: Int) {
        totalItems = total
        modifiedDate = OffsetDateTime.now()
    }

    fun getProgressPercentage(): Double {
        return if (totalItems == 0) 0.0 else (processedItems.toDouble() / totalItems.toDouble()) * 100.0
    }

    fun isCompleted(): Boolean = status == ArchiveStatus.COMPLETED

    fun isInProgress(): Boolean = status == ArchiveStatus.IN_PROGRESS

    fun isFailed(): Boolean = status == ArchiveStatus.FAILED

    fun canStart(): Boolean = status == ArchiveStatus.PENDING

    fun canCancel(): Boolean = status == ArchiveStatus.PENDING || status == ArchiveStatus.IN_PROGRESS
}
