package com.whale.api.archive.adapter.input.web.response

import com.whale.api.archive.domain.ArchiveItem
import java.time.OffsetDateTime

data class ArchiveItemPageResponse(
    val items: List<ArchiveItemResponse>,
    val hasNext: Boolean,
    val nextCursor: String?,
    val totalCount: Int,
) {
    companion object {
        fun from(
            items: List<ArchiveItem>,
            hasNext: Boolean,
            totalCount: Int,
        ): ArchiveItemPageResponse {
            val itemResponses = items.map { ArchiveItemResponse.from(it) }
            val nextCursor = if (hasNext && items.isNotEmpty()) {
                items.last().createdDate.toString()
            } else {
                null
            }
            
            return ArchiveItemPageResponse(
                items = itemResponses,
                hasNext = hasNext,
                nextCursor = nextCursor,
                totalCount = totalCount,
            )
        }
    }
}
