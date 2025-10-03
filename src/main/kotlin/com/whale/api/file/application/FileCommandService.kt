package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.SaveFileUseCase
import com.whale.api.file.application.port.`in`.command.SaveFileCommand
import com.whale.api.file.application.port.out.DeleteUnsortedFileOutput
import com.whale.api.file.application.port.out.FindFileHashOutput
import com.whale.api.file.application.port.out.FindSaveTaskOutput
import com.whale.api.file.application.port.out.FindTagOutput
import com.whale.api.file.application.port.out.SaveFileHashOutput
import com.whale.api.file.application.port.out.SaveFileOutput
import com.whale.api.file.application.port.out.SaveFileTagOutput
import com.whale.api.file.application.port.out.SaveSaveTaskOutput
import com.whale.api.file.application.port.out.SaveTagOutput
import com.whale.api.file.domain.File
import com.whale.api.file.domain.FileHash
import com.whale.api.file.domain.FileTag
import com.whale.api.file.domain.SaveTask
import com.whale.api.file.domain.Tag
import com.whale.api.file.domain.enums.SaveTaskStatus
import com.whale.api.file.domain.property.FileProperty
import com.whale.api.global.utils.Encoder.decodeBase64
import com.whale.api.global.utils.Encoder.encodeBase64
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID
import javax.imageio.ImageIO

@Service
class FileCommandService(
    private val fileProperty: FileProperty,
    private val findTagOutput: FindTagOutput,
    private val findFileHashOutput: FindFileHashOutput,
    private val findSaveTaskOutput: FindSaveTaskOutput,
    private val saveFileOutput: SaveFileOutput,
    private val saveTagOutput: SaveTagOutput,
    private val saveFileTagOutput: SaveFileTagOutput,
    private val saveSaveTaskOutput: SaveSaveTaskOutput,
    private val saveFileHashOutput: SaveFileHashOutput,
    private val deleteUnsortedFileOutput: DeleteUnsortedFileOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : SaveFileUseCase {

    private val logger = KotlinLogging.logger { }

    @Transactional
    override fun requestSave(command: SaveFileCommand) {
        logger.info("Saving file: {}", command)

        val now = OffsetDateTime.now()
        val task = SaveTask(
            identifier = UUID.randomUUID(),
            fileGroupIdentifier = command.fileGroupIdentifier,
            name = encodeBase64(command.name),
            path = encodeBase64(command.path),
            type = encodeBase64(command.type),
            tags = command.tags.map {
                SaveTask.Tag(
                    name = encodeBase64(it.name),
                    type = encodeBase64(it.type)
                )
            },
            sortOrder = command.sortOrder,
            status = SaveTaskStatus.PENDING,
            createdDate = now,
            modifiedDate = now
        )

        saveSaveTaskOutput.save(task)
    }

    override fun save() {
        val tasks = findSaveTaskOutput.findAllByStatus(SaveTaskStatus.PENDING)

        for (task in tasks) {
            save(task.identifier)
        }
    }

    private fun save(taskIdentifier: UUID) {
        logger.info("Saving file: {}", taskIdentifier)

        val task = writeTransactionTemplate.execute {
            val task = findSaveTaskOutput.findByIdForUpdate(taskIdentifier)
                ?: return@execute null

            if (task.status != SaveTaskStatus.PENDING) {
                logger.warn("Task is not pending: {}", taskIdentifier)
                return@execute null
            }

            task.start()

            saveSaveTaskOutput.save(task)
        } ?: return

        try {
            writeTransactionTemplate.execute {
                save(task)

                task.complete()
                saveSaveTaskOutput.save(task)
            }

        } catch (e: Exception) {
            logger.error("Failed to save file: {}", taskIdentifier, e)
            writeTransactionTemplate.execute {
                task.failed()
                saveSaveTaskOutput.save(task)
            }
        }

    }

    private fun save(task: SaveTask) {
        val fileHashValue = calculateFileHash(task.path)

        val file = writeTransactionTemplate.execute {
            if(findFileHashOutput.existByHash(fileHashValue)) {
                throw RuntimeException("File already exists with hash: $fileHashValue")
            }

            val now = OffsetDateTime.now()
            val file = File(
                identifier = UUID.randomUUID(),
                fileGroupIdentifier = task.fileGroupIdentifier,
                name = task.name,
                type = task.type,
                path = task.path,
                thumbnail = createThumbnail(task.path),
                sortOrder = task.sortOrder,
                createdDate = now,
                modifiedDate = now,
                lastViewDate = null
            )

            val fileHash = FileHash(
                identifier = UUID.randomUUID(),
                fileIdentifier = file.identifier,
                hash = fileHashValue
            )

            val tagByNameAndType = findTagOutput.findAllByNameInAndTypeIn(
                names = task.tags.map { it.name },
                types = task.tags.map { it.type }
            ).associateBy { it.name to it.type }

            val newTags = task.tags
                .filterNot { tagByNameAndType.containsKey(Pair(it.name, it.type)) }
                .map {
                    Tag(
                        identifier = UUID.randomUUID(),
                        name = it.name,
                        type = it.type
                    )
                }

            val totalTags: List<Tag> = tagByNameAndType.values + newTags

            val fileTags = totalTags.map {
                FileTag(
                    identifier = UUID.randomUUID(),
                    fileIdentifier = file.identifier,
                    tagIdentifier = it.identifier
                )
            }

            deleteUnsortedFileOutput.deleteByPath(task.path)
            saveFileOutput.save(file)
            saveTagOutput.saveAll(newTags)
            saveFileTagOutput.saveAll(fileTags)
            saveFileHashOutput.save(fileHash)
            file
        }!!

        moveFile(task.path, file.path)
    }

    private fun createThumbnail(path: String): String {
        val decodedPath = decodeBase64(path)
        val filePath = Paths.get(fileProperty.basePath).resolve(decodedPath)

        if (!Files.exists(filePath)) {
            throw RuntimeException("File not found: $decodedPath")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        val isImage = extension in fileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }
        val isVideo = extension in fileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") }

        if (!isImage && !isVideo) {
            throw RuntimeException("Unsupported media type: $extension")
        }

        val thumbnailRelativePath = "${fileProperty.thumbnailPath}/${decodedPath}.thumbnail.jpg"
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

    private fun calculateFileHash(path: String): String {
        val decodedPath = decodeBase64(path)
        val filePath = Paths.get(fileProperty.basePath).resolve(decodedPath)

        if (!Files.exists(filePath)) {
            throw RuntimeException("File not found: $decodedPath")
        }

        val extension = filePath.toString().substringAfterLast('.').lowercase()
        val isImage = extension in fileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }
        val isVideo = extension in fileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") }

        return when {
            isImage -> hashImage(filePath)
            isVideo -> hashVideo(filePath)
            else -> throw RuntimeException("Unsupported media type: $extension")
        }
    }

    private fun moveFile(sourcePath: String, destinationPath: String) {
        val decodedSourcePath = decodeBase64(sourcePath)
        val decodedDestinationPath = decodeBase64(destinationPath)

        val sourceFile = Paths.get(fileProperty.basePath).resolve(decodedSourcePath)
        val destinationFile = Paths.get(fileProperty.basePath).resolve(decodedDestinationPath)

        if (!Files.exists(sourceFile)) {
            throw RuntimeException("Source file not found: $decodedSourcePath")
        }

        // 대상 경로의 디렉토리가 존재하지 않으면 생성
        Files.createDirectories(destinationFile.parent)

        // 파일 이동
        Files.move(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        logger.info("Moved '{}' to '{}'", decodedSourcePath, decodedDestinationPath)
    }

    private fun generateImageThumbnail(filePath: Path, thumbnailPath: Path) {
        val originalImage = ImageIO.read(filePath.toFile())
        val thumbnailImage = createThumbnail(originalImage, 512, 512)

        ImageIO.write(thumbnailImage, "jpg", thumbnailPath.toFile())
    }

    private fun generateVideoThumbnail(filePath: Path, thumbnailPath: Path) {
        // FFmpeg를 사용한 비디오 썸네일 생성 (1초 지점에서 512px 너비로)
        val processBuilder = ProcessBuilder(
            "ffmpeg",
            "-i", filePath.toString(),
            "-ss", "1",
            "-vframes", "1",
            "-vf", "scale=512:-1",
            "-y",
            thumbnailPath.toString()
        )

        val process = processBuilder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0 || !Files.exists(thumbnailPath) || Files.size(thumbnailPath) == 0L) {
            throw RuntimeException("FFmpeg failed to generate thumbnail, exit code: $exitCode")
        }
    }

    private fun createThumbnail(originalImage: BufferedImage, width: Int, height: Int): BufferedImage {
        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val thumbnailImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = thumbnailImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()
        return thumbnailImage
    }

    private fun hashImage(filePath: Path, size: Pair<Int, Int> = Pair(256, 256)): String {
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

    private fun hashVideo(filePath: Path, frameRate: Int = 10, size: Pair<Int, Int> = Pair(256, 256)): String {
        return try {
            val hasher = MessageDigest.getInstance("SHA-256")

            // FFmpeg 명령어: 10프레임마다 샘플링, 256x256 그레이스케일로 변환
            val processBuilder = ProcessBuilder(
                "ffmpeg",
                "-i", filePath.toString(),
                "-vf", "select='not(mod(n,$frameRate))',scale=${size.first}:${size.second}",
                "-pix_fmt", "gray",
                "-f", "rawvideo",
                "-vcodec", "rawvideo",
                "-"
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
}
