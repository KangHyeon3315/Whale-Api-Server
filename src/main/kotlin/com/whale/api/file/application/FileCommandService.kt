package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.SaveFileUseCase
import com.whale.api.file.application.port.`in`.command.SaveFileCommand
import com.whale.api.file.application.port.out.CreateThumbnailOutput
import com.whale.api.file.application.port.out.DeleteUnsortedFileOutput
import com.whale.api.file.application.port.out.FindFileHashOutput
import com.whale.api.file.application.port.out.FindSaveTaskOutput
import com.whale.api.file.application.port.out.FindTagOutput
import com.whale.api.file.application.port.out.HashFileOutput
import com.whale.api.file.application.port.out.MoveFileOutput
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
    private val findTagOutput: FindTagOutput,
    private val findFileHashOutput: FindFileHashOutput,
    private val findSaveTaskOutput: FindSaveTaskOutput,
    private val saveFileOutput: SaveFileOutput,
    private val saveTagOutput: SaveTagOutput,
    private val saveFileTagOutput: SaveFileTagOutput,
    private val saveSaveTaskOutput: SaveSaveTaskOutput,
    private val saveFileHashOutput: SaveFileHashOutput,
    private val moveFileOutput: MoveFileOutput,
    private val deleteUnsortedFileOutput: DeleteUnsortedFileOutput,
    private val hashFileOutput: HashFileOutput,
    private val createThumbnailOutput: CreateThumbnailOutput,
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
        val fileHashValue = hashFileOutput.calculateFileHash(
            decodeBase64(task.path)
        )

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
                thumbnail = createThumbnailOutput.createThumbnail(
                    decodeBase64(task.path)
                ),
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

        moveFileOutput.moveFile(
            sourcePath = decodeBase64(task.path),
            destinationPath = decodeBase64(file.path)
        )
    }
}
