package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveMetadata
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface MetadataExtractionOutput {
    fun extractMetadata(file: MultipartFile, archiveItemIdentifier: UUID): List<ArchiveMetadata>
    fun extractImageMetadata(file: MultipartFile, archiveItemIdentifier: UUID): List<ArchiveMetadata>
    fun extractVideoMetadata(file: MultipartFile, archiveItemIdentifier: UUID): List<ArchiveMetadata>
    fun extractTextMetadata(file: MultipartFile, archiveItemIdentifier: UUID): List<ArchiveMetadata>
    fun extractDocumentMetadata(file: MultipartFile, archiveItemIdentifier: UUID): List<ArchiveMetadata>
    fun extractLivePhotoMetadata(
        imageFile: MultipartFile,
        videoFile: MultipartFile?,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>
}
