package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveMetadata
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface MetadataExtractionOutput {
    fun extractMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    fun extractImageMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    fun extractVideoMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    fun extractTextMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    fun extractDocumentMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    fun extractLivePhotoMetadata(
        imageFile: MultipartFile,
        videoFile: MultipartFile?,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>

    /**
     * 저장된 파일에서 메타데이터를 추출합니다. (메모리 효율적)
     *
     * @param filePath 저장된 파일 경로
     * @param fileName 파일 이름
     * @param mimeType MIME 타입
     * @param fileSize 파일 크기
     * @param archiveItemIdentifier 아카이브 아이템 식별자
     * @return 추출된 메타데이터 리스트
     */
    fun extractMetadataFromFile(
        filePath: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata>
}
