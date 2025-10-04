package com.whale.api.file.adapter.output.file

import com.whale.api.file.application.port.out.CreateThumbnailOutput
import com.whale.api.file.application.port.out.DeleteFileOutput
import com.whale.api.file.application.port.out.FileInfo
import com.whale.api.file.application.port.out.HashFileOutput
import com.whale.api.file.application.port.out.ListDirectoryOutput
import com.whale.api.file.application.port.out.MoveFileOutput
import com.whale.api.file.application.port.out.ReadFileOutput
import com.whale.api.file.application.port.out.ScanDirectoryOutput
import com.whale.api.file.application.port.out.ValidateFilePathOutput
import com.whale.api.file.domain.FileResource
import com.whale.api.file.domain.FileTreeItem
import com.whale.api.file.domain.exception.InvalidPathException
import com.whale.api.file.domain.property.FileProperty
import com.whale.api.global.exception.BusinessException
import com.whale.api.global.utils.Encoder.encodeBase64
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Repository
class FileAdapter(
    private val fileProperty: FileProperty,
) : HashFileOutput,
    MoveFileOutput,
    CreateThumbnailOutput,
    ValidateFilePathOutput,
    ReadFileOutput,
    DeleteFileOutput,
    ScanDirectoryOutput,
    ListDirectoryOutput {
    private val logger = KotlinLogging.logger {}

    private fun hashVideo(
        filePath: Path,
        frameRate: Int = 10,
        size: Pair<Int, Int> = Pair(256, 256),
    ): String {
        return try {
            val hasher = MessageDigest.getInstance("SHA-256")

            // FFmpeg 명령어: 10프레임마다 샘플링, 256x256 그레이스케일로 변환
            val processBuilder =
                ProcessBuilder(
                    "ffmpeg",
                    "-i", filePath.toString(),
                    "-vf", "select='not(mod(n,$frameRate))',scale=${size.first}:${size.second}",
                    "-pix_fmt", "gray",
                    "-f", "rawvideo",
                    "-vcodec", "rawvideo",
                    "-",
                )

            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD) // stderr 무시
            val process = processBuilder.start()

            // 각 프레임은 width * height 바이트 (그레이스케일)
            val frameSize = size.first * size.second
            val frameBuffer = ByteArray(frameSize)

            process.inputStream.use { inputStream ->
                var totalBytesRead = 0
                while (true) {
                    val bytesRead = inputStream.read(frameBuffer)
                    if (bytesRead == -1) break

                    if (bytesRead == frameSize) {
                        // 완전한 프레임을 읽었을 때만 해시 업데이트
                        hasher.update(frameBuffer)
                        totalBytesRead += bytesRead
                    } else if (bytesRead > 0) {
                        // 부분적인 프레임 데이터도 처리 (마지막 프레임일 수 있음)
                        hasher.update(frameBuffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                logger.warn("FFmpeg exited with code $exitCode for video hash calculation")
            }

            hasher.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logger.error("Failed to calculate hash for video: $filePath", e)
            throw RuntimeException("Failed to calculate video hash", e)
        }
    }

    private fun hashImage(
        filePath: Path,
        size: Pair<Int, Int> = Pair(256, 256),
    ): String {
        val originalImage = ImageIO.read(filePath.toFile())
        val resizedImage = originalImage.getScaledInstance(size.first, size.second, Image.SCALE_SMOOTH)

        // 그레이스케일로 변환
        val grayscaleImage = BufferedImage(size.first, size.second, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = grayscaleImage.createGraphics()
        graphics.drawImage(resizedImage, 0, 0, null)
        graphics.dispose()

        // 이미지를 바이트 데이터로 변환
        val raster = grayscaleImage.raster
        val imageBytes = ByteArray(size.first * size.second)
        raster.getDataElements(0, 0, size.first, size.second, imageBytes)

        // SHA-256 해싱
        val hasher = MessageDigest.getInstance("SHA-256")
        hasher.update(imageBytes)
        return hasher.digest().joinToString("") { "%02x".format(it) }
    }

    private fun generateVideoThumbnail(
        filePath: Path,
        thumbnailPath: Path,
    ) {
        // FFmpeg를 사용한 비디오 썸네일 생성 (1초 지점에서 512px 너비로)
        val processBuilder =
            ProcessBuilder(
                "ffmpeg",
                "-i", filePath.toString(),
                "-ss", "1",
                "-vframes", "1",
                "-vf", "scale=512:-1",
                "-y",
                thumbnailPath.toString(),
            )

        val process = processBuilder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0 || !Files.exists(thumbnailPath) || Files.size(thumbnailPath) == 0L) {
            throw RuntimeException("FFmpeg failed to generate thumbnail, exit code: $exitCode")
        }
    }

    private fun generateImageThumbnail(
        filePath: Path,
        thumbnailPath: Path,
    ) {
        val originalImage = ImageIO.read(filePath.toFile())
        val thumbnailImage = createThumbnail(originalImage, 512, 512)

        ImageIO.write(thumbnailImage, "jpg", thumbnailPath.toFile())
    }

    private fun createThumbnail(
        originalImage: BufferedImage,
        width: Int,
        height: Int,
    ): BufferedImage {
        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val thumbnailImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = thumbnailImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()
        return thumbnailImage
    }

    override fun moveFile(
        sourcePath: String,
        destinationPath: String,
    ) {
        val sourceFile = Paths.get(fileProperty.basePath).resolve(sourcePath)
        val destinationFile = Paths.get(fileProperty.basePath).resolve(destinationPath)

        if (!Files.exists(sourceFile)) {
            throw RuntimeException("Source file not found: $sourcePath")
        }

        // 대상 경로의 디렉토리가 존재하지 않으면 생성
        Files.createDirectories(destinationFile.parent)

        // 파일 이동
        Files.move(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        logger.info("Moved '{}' to '{}'", sourcePath, destinationPath)
    }

    override fun calculateFileHash(path: String): String {
        val filePath = Paths.get(fileProperty.basePath).resolve(path)

        if (!Files.exists(filePath)) {
            throw RuntimeException("File not found: $path")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        val isImage = extension in fileProperty.imageExtensions.map { it.removePrefix(".") }
        val isVideo = extension in fileProperty.videoExtensions.map { it.removePrefix(".") }

        return when {
            isImage -> hashImage(filePath)
            isVideo -> hashVideo(filePath)
            else -> throw RuntimeException("Unsupported media type: $extension")
        }
    }

    override fun createThumbnail(path: String): String {
        val filePath = Paths.get(fileProperty.basePath).resolve(path)

        if (!Files.exists(filePath)) {
            throw RuntimeException("File not found: $path")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        val isImage = extension in fileProperty.imageExtensions.map { it.removePrefix(".") }
        val isVideo = extension in fileProperty.videoExtensions.map { it.removePrefix(".") }

        if (!isImage && !isVideo) {
            throw RuntimeException("Unsupported media type: $extension")
        }

        val thumbnailRelativePath = "${fileProperty.thumbnailPath}/$path.thumbnail.jpg"
        val thumbnailPath = Paths.get(fileProperty.basePath).resolve(thumbnailRelativePath)

        // 썸네일 디렉토리 생성
        Files.createDirectories(thumbnailPath.parent)

        // 이미 썸네일이 존재하면 경로 반환
        if (Files.exists(thumbnailPath)) {
            logger.info("Returning existing thumbnail: $thumbnailPath")
            return encodeBase64(thumbnailRelativePath)
        }

        try {
            if (isImage) {
                generateImageThumbnail(filePath, thumbnailPath)
            } else {
                generateVideoThumbnail(filePath, thumbnailPath)
            }

            logger.info("Generated new thumbnail: $thumbnailPath")
            return encodeBase64(thumbnailRelativePath)
        } catch (e: Exception) {
            logger.error("Error generating thumbnail: ${e.message}", e)
            throw RuntimeException("Failed to generate thumbnail", e)
        }
    }

    // ValidateFilePathOutput 구현
    override fun validatePath(path: String) {
        // Path traversal 체크
        if (".." in path) {
            throw InvalidPathException()
        }

        val fullPath = Paths.get(fileProperty.basePath, path)
        if (!Files.exists(fullPath)) {
            throw InvalidPathException()
        }
    }

    override fun isImageFile(path: String): Boolean {
        val extension = Paths.get(path).toFile().extension.lowercase()
        return fileProperty.imageExtensions.contains(".$extension")
    }

    override fun isVideoFile(path: String): Boolean {
        val extension = Paths.get(path).toFile().extension.lowercase()
        return fileProperty.videoExtensions.contains(".$extension")
    }

    // ReadFileOutput 구현
    override fun readFile(path: String): FileResource {
        val file = Paths.get(fileProperty.basePath, path).toFile()
        val extension = file.extension.lowercase()
        val mimeType = fileProperty.mimeTypeMapping[".$extension"] ?: "application/octet-stream"

        return FileResource(
            path = path,
            mimeType = mimeType,
            size = file.length(),
            inputStream = FileInputStream(file),
        )
    }

    override fun readFileWithRange(
        path: String,
        rangeHeader: String,
    ): FileResource {
        val file = Paths.get(fileProperty.basePath, path).toFile()
        val extension = file.extension.lowercase()
        val mimeType = fileProperty.mimeTypeMapping[".$extension"] ?: "application/octet-stream"
        val fileSize = file.length()

        val rangePattern = Pattern.compile("bytes=(\\d+)-(\\d*)")
        val matcher = rangePattern.matcher(rangeHeader)

        if (!matcher.matches()) {
            throw BusinessException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value(), "Invalid Range header")
        }

        val start = matcher.group(1).toLong()
        val endGroup = matcher.group(2)
        val end = if (endGroup.isNotEmpty()) endGroup.toLong() else fileSize - 1

        if (start >= fileSize || end >= fileSize) {
            throw BusinessException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value(), "Requested Range Not Satisfiable")
        }

        // Range 요청을 위한 특별한 InputStream 생성
        val inputStream =
            object : FileInputStream(file) {
                private var skipped = false

                override fun read(): Int {
                    if (!skipped) {
                        skip(start)
                        skipped = true
                    }
                    return super.read()
                }

                override fun read(b: ByteArray): Int {
                    if (!skipped) {
                        skip(start)
                        skipped = true
                    }
                    return super.read(b)
                }

                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int {
                    if (!skipped) {
                        skip(start)
                        skipped = true
                    }
                    return super.read(b, off, len)
                }
            }

        return FileResource(
            path = path,
            mimeType = mimeType,
            size = fileSize,
            inputStream = inputStream,
            isRangeRequest = true,
            rangeStart = start,
            rangeEnd = end,
        )
    }

    // DeleteFileOutput 구현
    override fun deleteFile(path: String) {
        val fullPath = Paths.get(path)

        if (!Files.exists(fullPath)) {
            throw RuntimeException("File not found: $path")
        }

        try {
            Files.delete(fullPath)
            logger.info("Successfully deleted file: $path")
        } catch (e: Exception) {
            logger.error("Failed to delete file: $path", e)
            throw RuntimeException("Failed to delete file: $path", e)
        }
    }

    // ScanDirectoryOutput 구현
    override fun scanDirectory(directoryPath: String): List<FileInfo> {
        val dirPath = Paths.get(directoryPath)
        val basePath = Paths.get(fileProperty.basePath)
        val fileInfoList = mutableListOf<FileInfo>()

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw RuntimeException("Directory not found or not a directory: $directoryPath")
        }

        try {
            Files.walk(dirPath)
                .filter { Files.isRegularFile(it) }
                .forEach { filePath ->
                    val fileName = filePath.fileName.toString()
                    val extension = fileName.substringAfterLast('.', "").lowercase()
                    val relativePath = basePath.relativize(filePath).toString()

                    // 지원하는 파일 확장자만 처리
                    if (extension.isNotEmpty() &&
                        (
                            fileProperty.videoExtensions.contains(".$extension") ||
                                fileProperty.imageExtensions.contains(".$extension")
                        )
                    ) {
                        fileInfoList.add(
                            FileInfo(
                                path = filePath,
                                relativePath = relativePath,
                                name = fileName,
                                extension = extension,
                            ),
                        )
                    }
                }
        } catch (e: Exception) {
            logger.error("Error scanning directory: $directoryPath", e)
            throw RuntimeException("Failed to scan directory: $directoryPath", e)
        }

        return fileInfoList
    }

    override fun getVideoEncoding(filePath: String): String? {
        // 비디오 인코딩 정보를 추출하는 로직
        // Python 코드의 get_video_encoding_from_header 함수와 동일한 기능
        // 실제 구현에서는 FFmpeg 라이브러리나 다른 미디어 라이브러리를 사용해야 함
        // 여기서는 간단한 구현으로 대체
        try {
            val path = Paths.get(filePath)
            if (!Files.exists(path)) {
                return null
            }

            // 실제로는 FFmpeg나 다른 라이브러리를 사용해서 비디오 인코딩 정보를 추출해야 함
            // 여기서는 예시로 파일 확장자 기반으로 간단히 처리
            val extension = path.toString().substringAfterLast('.').lowercase()
            return when (extension) {
                "mp4" -> "h264"
                "avi" -> "xvid"
                "mkv" -> "h264"
                "mov" -> "h264"
                else -> "unknown"
            }
        } catch (e: Exception) {
            logger.error("Error getting video encoding for: $filePath", e)
            return null
        }
    }

    // ListDirectoryOutput 구현
    override fun listDirectory(directoryPath: String): List<FileTreeItem> {
        val dirPath = Paths.get(fileProperty.basePath, directoryPath)
        val fileTreeItems = mutableListOf<FileTreeItem>()

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw RuntimeException("Directory not found or not a directory: $directoryPath")
        }

        try {
            Files.list(dirPath).use { stream ->
                stream.forEach { filePath ->
                    val fileName = filePath.fileName.toString()
                    val isDir = Files.isDirectory(filePath)
                    val extension =
                        if (!isDir) {
                            fileName.substringAfterLast('.', "").lowercase()
                        } else {
                            ""
                        }

                    fileTreeItems.add(
                        FileTreeItem(
                            name = fileName,
                            isDir = isDir,
                            extension = extension,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Error listing directory: $directoryPath", e)
            throw RuntimeException("Failed to list directory: $directoryPath", e)
        }

        return fileTreeItems
    }
}
