package com.whale.api.archive.adapter.output.file

import com.whale.api.archive.application.port.out.FileStorageOutput
import com.whale.api.archive.application.port.out.ReadArchiveItemContentOutput
import com.whale.api.archive.domain.property.ArchiveProperty
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

@Repository
class ArchiveFileAdapter(
    private val archiveProperty: ArchiveProperty,
) : FileStorageOutput,
    ReadArchiveItemContentOutput {

    private val logger = KotlinLogging.logger {}

    override fun storeFile(file: MultipartFile, relativePath: String): String {
        val targetPath = Paths.get(archiveProperty.basePath, relativePath, file.originalFilename ?: "unknown")

        try {
            // 디렉토리 생성
            Files.createDirectories(targetPath.parent)

            // 파일 저장
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.info { "File stored successfully: $targetPath" }
            return targetPath.toString()
        } catch (e: Exception) {
            logger.error(e) { "Failed to store file: $relativePath" }
            throw RuntimeException("Failed to store file", e)
        }
    }

    override fun storeFile(inputStream: InputStream, fileName: String, relativePath: String): String {
        val targetPath = Paths.get(archiveProperty.basePath, relativePath, fileName)

        try {
            // 디렉토리 생성
            Files.createDirectories(targetPath.parent)

            // 파일 저장
            inputStream.use {
                Files.copy(it, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.info { "File stored successfully: $targetPath" }
            return targetPath.toString()
        } catch (e: Exception) {
            logger.error(e) { "Failed to store file: $relativePath/$fileName" }
            throw RuntimeException("Failed to store file", e)
        }
    }

    override fun deleteFile(filePath: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            Files.deleteIfExists(path)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete file: $filePath" }
            false
        }
    }



    override fun getFileSize(filePath: String): Long {
        return try {
            Files.size(Paths.get(filePath))
        } catch (e: Exception) {
            logger.error(e) { "Failed to get file size: $filePath" }
            0L
        }
    }

    override fun calculateChecksum(file: MultipartFile): String {
        return file.inputStream.use { inputStream ->
            calculateChecksum(inputStream)
        }
    }

    override fun calculateChecksum(inputStream: InputStream): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logger.error(e) { "Failed to calculate checksum" }
            throw RuntimeException("Failed to calculate checksum", e)
        }
    }

    override fun readTextContent(storedPath: String): String {
        return try {
            val path = Paths.get(storedPath)
            Files.readString(path, Charsets.UTF_8)
        } catch (e: Exception) {
            logger.error(e) { "Failed to read text content: $storedPath" }
            throw RuntimeException("Failed to read text content", e)
        }
    }

    override fun readTextContentPreview(storedPath: String, maxLength: Int): String {
        return try {
            val path = Paths.get(storedPath)
            val content = Files.readString(path, Charsets.UTF_8)
            if (content.length <= maxLength) {
                content
            } else {
                content.substring(0, maxLength) + "..."
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read text content preview: $storedPath" }
            throw RuntimeException("Failed to read text content preview", e)
        }
    }

    override fun fileExists(storedPath: String): Boolean {
        return Files.exists(Paths.get(storedPath))
    }
}
