package com.whale.api.archive.application.port.out

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

data class StreamingFileResult(
    val storedPath: String,
    val checksum: String,
    val fileSize: Long,
)

interface FileStorageOutput {
    fun storeFile(
        file: MultipartFile,
        relativePath: String,
    ): String

    fun storeFile(
        inputStream: InputStream,
        fileName: String,
        relativePath: String,
    ): String

    /**
     * 스트리밍 방식으로 파일을 저장하면서 동시에 체크섬을 계산합니다.
     * 한 번의 스트림 읽기로 파일 저장과 체크섬 계산을 수행하여 메모리 효율적입니다.
     *
     * @param inputStream 파일 입력 스트림
     * @param fileName 파일 이름
     * @param relativePath 상대 경로
     * @return 저장된 파일 경로, 체크섬, 파일 크기
     */
    fun storeFileWithChecksum(
        inputStream: InputStream,
        fileName: String,
        relativePath: String,
    ): StreamingFileResult

    fun deleteFile(filePath: String): Boolean

    fun fileExists(filePath: String): Boolean

    fun getFileSize(filePath: String): Long

    fun calculateChecksum(file: MultipartFile): String

    fun calculateChecksum(inputStream: InputStream): String

    /**
     * 저장된 파일에서 체크섬을 계산합니다.
     *
     * @param filePath 파일 경로
     * @return SHA-256 체크섬
     */
    fun calculateChecksumFromFile(filePath: String): String
}
