package com.whale.api.service.file.util

import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

@Component
class ThumbnailUtil {
    
    private val logger = KotlinLogging.logger {}
    
    fun generateImageThumbnail(filePath: Path, thumbnailPath: Path, width: Int = 512, height: Int = 512): ResponseEntity<Resource> {
        return try {
            Files.createDirectories(thumbnailPath.parent)

            val originalImage = ImageIO.read(filePath.toFile())
            val thumbnailImage = createThumbnail(originalImage, width, height)

            ImageIO.write(thumbnailImage, "jpg", thumbnailPath.toFile())

            val resource = FileSystemResource(thumbnailPath)
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource)
        } catch (e: Exception) {
            logger.error("Error generating image thumbnail for $filePath", e)
            throw RuntimeException("Failed to generate image thumbnail", e)
        }
    }
    
    fun generateVideoThumbnail(filePath: Path, thumbnailPath: Path, width: Int = 512): ResponseEntity<Resource> {
        return try {
            Files.createDirectories(thumbnailPath.parent)

            // FFmpeg를 사용한 비디오 썸네일 생성
            val processBuilder = ProcessBuilder(
                "ffmpeg",
                "-i", filePath.toString(),
                "-ss", "1",
                "-vframes", "1",
                "-vf", "scale=$width:-1",
                "-y",
                thumbnailPath.toString()
            )

            val process = processBuilder.start()
            val exitCode = process.waitFor()

            if (exitCode == 0 && Files.exists(thumbnailPath) && Files.size(thumbnailPath) > 0) {
                val resource = FileSystemResource(thumbnailPath)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(resource)
            } else {
                logger.error("FFmpeg failed to generate thumbnail for $filePath, exit code: $exitCode")
                throw RuntimeException("Failed to generate video thumbnail")
            }
        } catch (e: Exception) {
            logger.error("Error generating video thumbnail for $filePath", e)
            throw RuntimeException("Failed to generate video thumbnail", e)
        }
    }
    
    fun thumbnailExists(thumbnailPath: Path): Boolean {
        return Files.exists(thumbnailPath)
    }
    
    fun getThumbnailResource(thumbnailPath: Path): ResponseEntity<Resource> {
        val resource = FileSystemResource(thumbnailPath)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
            .body(resource)
    }
    
    private fun createThumbnail(originalImage: BufferedImage, width: Int, height: Int): BufferedImage {
        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val thumbnailImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = thumbnailImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()
        return thumbnailImage
    }
}
