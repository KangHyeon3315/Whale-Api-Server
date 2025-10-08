package com.whale.api.archive.adapter.output.file

import com.whale.api.archive.application.port.out.FileStorageOutput
import com.whale.api.archive.application.port.out.ReadArchiveFileOutput
import com.whale.api.archive.application.port.out.ReadArchiveItemContentOutput
import com.whale.api.archive.domain.ArchiveFileResource
import com.whale.api.archive.domain.property.ArchiveProperty
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Repository
class ArchiveFileAdapter(
    private val archiveProperty: ArchiveProperty,
) : FileStorageOutput,
    ReadArchiveItemContentOutput,
    ReadArchiveFileOutput {
    private val logger = KotlinLogging.logger {}

    override fun storeFile(
        file: MultipartFile,
        relativePath: String,
    ): String {
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

    override fun storeFile(
        inputStream: InputStream,
        fileName: String,
        relativePath: String,
    ): String {
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

    override fun readTextContentPreview(
        storedPath: String,
        maxLength: Int,
    ): String {
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

    // ReadArchiveFileOutput 구현
    override fun readArchiveFile(
        storedPath: String,
        fileName: String,
        mimeType: String,
    ): ArchiveFileResource {
        val file = Paths.get(storedPath).toFile()

        if (!file.exists()) {
            throw RuntimeException("Archive file not found: $storedPath")
        }

        return ArchiveFileResource(
            // 실제로는 파라미터로 받아야 함
            itemIdentifier = UUID.randomUUID().toString(),
            fileName = fileName,
            mimeType = mimeType,
            size = file.length(),
            inputStream = FileInputStream(file),
        )
    }

    override fun readArchiveFileWithRange(
        storedPath: String,
        fileName: String,
        mimeType: String,
        rangeHeader: String,
    ): ArchiveFileResource {
        val file = Paths.get(storedPath).toFile()

        if (!file.exists()) {
            throw RuntimeException("Archive file not found: $storedPath")
        }

        val fileSize = file.length()
        val rangePattern = Pattern.compile("bytes=(\\d+)-(\\d*)")
        val matcher = rangePattern.matcher(rangeHeader)

        if (!matcher.matches()) {
            throw RuntimeException("Invalid Range header: $rangeHeader")
        }

        val start = matcher.group(1).toLong()
        val end = if (matcher.group(2).isEmpty()) fileSize - 1 else matcher.group(2).toLong()

        if (start >= fileSize || end >= fileSize || start > end) {
            throw RuntimeException("Invalid range: $start-$end for file size $fileSize")
        }

        val inputStream = FileInputStream(file)
        inputStream.skip(start)

        return ArchiveFileResource(
            itemIdentifier = UUID.randomUUID().toString(),
            fileName = fileName,
            mimeType = mimeType,
            size = fileSize,
            inputStream = inputStream,
            isRangeRequest = true,
            rangeStart = start,
            rangeEnd = end,
        )
    }

    override fun createThumbnail(
        storedPath: String,
        mimeType: String,
    ): String {
        val thumbnailDir = Paths.get(archiveProperty.basePath, archiveProperty.thumbnailPath)
        Files.createDirectories(thumbnailDir)

        val originalFile = Paths.get(storedPath)
        val fileName = originalFile.fileName.toString()
        val nameWithoutExt = fileName.substringBeforeLast('.')
        val thumbnailFileName = "${nameWithoutExt}_thumb.jpg"
        val thumbnailPath = thumbnailDir.resolve(thumbnailFileName)

        // 썸네일이 이미 존재하면 기존 것 사용
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath.toString()
        }

        try {
            if (isImageFile(mimeType)) {
                createImageThumbnail(storedPath, thumbnailPath.toString())
            } else if (isVideoFile(mimeType)) {
                createVideoThumbnail(storedPath, thumbnailPath.toString())
            } else {
                throw RuntimeException("Unsupported file type for thumbnail: $mimeType")
            }

            return thumbnailPath.toString()
        } catch (e: Exception) {
            logger.error(e) { "Failed to create thumbnail for: $storedPath" }
            throw RuntimeException("Failed to create thumbnail", e)
        }
    }

    private fun createImageThumbnail(
        imagePath: String,
        thumbnailPath: String,
    ) {
        val originalImage = ImageIO.read(Paths.get(imagePath).toFile())
        val thumbnailSize = 200

        val scaledImage = originalImage.getScaledInstance(thumbnailSize, thumbnailSize, BufferedImage.SCALE_SMOOTH)
        val thumbnailImage = BufferedImage(thumbnailSize, thumbnailSize, BufferedImage.TYPE_INT_RGB)

        val graphics = thumbnailImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        ImageIO.write(thumbnailImage, "jpg", Paths.get(thumbnailPath).toFile())
    }

    private fun createVideoThumbnail(
        videoPath: String,
        thumbnailPath: String,
    ) {
        // FFmpeg를 사용한 비디오 썸네일 생성
        val processBuilder =
            ProcessBuilder(
                "ffmpeg",
                "-i", videoPath,
                "-ss", "00:00:01.000",
                "-vframes", "1",
                "-s", "200x200",
                "-y",
                thumbnailPath,
            )

        val process = processBuilder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("Failed to create video thumbnail. FFmpeg exit code: $exitCode")
        }
    }

    override fun readThumbnail(
        thumbnailPath: String,
        fileName: String,
    ): ArchiveFileResource {
        val file = Paths.get(thumbnailPath).toFile()

        if (!file.exists()) {
            throw RuntimeException("Thumbnail not found: $thumbnailPath")
        }

        return ArchiveFileResource(
            itemIdentifier = UUID.randomUUID().toString(),
            fileName = fileName,
            mimeType = "image/jpeg",
            size = file.length(),
            inputStream = FileInputStream(file),
        )
    }

    override fun validateFilePath(storedPath: String): Boolean {
        val path = Paths.get(storedPath)
        return Files.exists(path) && Files.isRegularFile(path)
    }

    override fun isImageFile(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun isVideoFile(mimeType: String): Boolean {
        return mimeType.startsWith("video/")
    }
}
