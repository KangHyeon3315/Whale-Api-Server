package com.whale.api.archive.application.port.`in`.command

import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.UUID

data class UploadArchiveItemCommand(
    val archiveIdentifier: UUID,
    val file: MultipartFile,
    val originalPath: String,
    val isLivePhoto: Boolean,
    val livePhotoVideo: MultipartFile?,
    val originalCreatedDate: OffsetDateTime?,
    val originalModifiedDate: OffsetDateTime?,
    val metadata: Map<String, String> = emptyMap(),
)
