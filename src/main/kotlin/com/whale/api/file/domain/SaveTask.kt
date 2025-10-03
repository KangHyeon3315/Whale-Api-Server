package com.whale.api.file.domain

import com.whale.api.file.domain.enums.SaveTaskStatus
import java.time.OffsetDateTime
import java.util.UUID

class SaveTask(
    val identifier: UUID,
    val fileGroupIdentifier: UUID?,
    val name: String,
    val path: String,
    val type: String,
    val sortOrder: Int?,
    val tags: List<Tag>,
    var status: SaveTaskStatus,
    val createdDate: OffsetDateTime,
    var modifiedDate: OffsetDateTime
) {

    class Tag(
        val name: String,
        val type: String
    )

    fun failed() {
        status = SaveTaskStatus.FAILED
        modifiedDate = OffsetDateTime.now()
    }

    fun start() {
        status = SaveTaskStatus.PROCESSING
        modifiedDate = OffsetDateTime.now()
    }

    fun complete() {
        status = SaveTaskStatus.COMPLETED
        modifiedDate = OffsetDateTime.now()
    }
}
