package com.whale.api.archive.domain

import java.time.OffsetDateTime
import java.util.UUID

class Archive(
    val identifier: UUID,
    val name: String,
    val description: String?,
    var totalItems: Int,
    var processedItems: Int,
    var failedItems: Int,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime,
    var completedDate: OffsetDateTime?,
) {
    fun incrementProcessedItems() {
        processedItems++
        modifiedDate = OffsetDateTime.now()
    }

    fun decrementProcessedItems() {
        if (processedItems > 0) {
            processedItems--
            modifiedDate = OffsetDateTime.now()
        }
    }

    fun incrementProcessedItemsAndCheckCompletion(): Boolean {
        processedItems++
        modifiedDate = OffsetDateTime.now()

        // totalItems가 설정되어 있고, 모든 아이템 처리 완료 시 자동 완료
        if (totalItems > 0 && (processedItems + failedItems) >= totalItems && completedDate == null) {
            completedDate = OffsetDateTime.now()
            return true
        }

        return false
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

    fun isCompleted(): Boolean = completedDate != null
}
