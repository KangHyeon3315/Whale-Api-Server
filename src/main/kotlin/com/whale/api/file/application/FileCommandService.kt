package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.SaveFileUseCase
import com.whale.api.file.application.port.`in`.command.SaveFileCommand
import com.whale.api.file.application.port.out.CreateThumbnailOutput
import com.whale.api.file.application.port.out.DeleteFileEntityOutput
import com.whale.api.file.application.port.out.DeleteFileHashOutput
import com.whale.api.file.application.port.out.DeleteFileTagOutput
import com.whale.api.file.application.port.out.DeleteTagOutput
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
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.UUID

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
    private val moveFileOutput: MoveFileOutput,
    private val deleteUnsortedFileOutput: DeleteUnsortedFileOutput,
    private val deleteFileEntityOutput: DeleteFileEntityOutput,
    private val deleteFileHashOutput: DeleteFileHashOutput,
    private val deleteFileTagOutput: DeleteFileTagOutput,
    private val deleteTagOutput: DeleteTagOutput,
    private val hashFileOutput: HashFileOutput,
    private val createThumbnailOutput: CreateThumbnailOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : SaveFileUseCase {
    private val logger = KotlinLogging.logger { }

    @Transactional
    override fun requestSave(command: SaveFileCommand) {
        logger.info("Saving file: {}", command)

        val now = OffsetDateTime.now()
        val task =
            SaveTask(
                identifier = UUID.randomUUID(),
                fileGroupIdentifier = command.fileGroupIdentifier,
                name = encodeBase64(command.name),
                path = encodeBase64(command.path),
                type = encodeBase64(command.type),
                tags =
                    command.tags.map {
                        SaveTask.Tag(
                            name = encodeBase64(it.name),
                            type = encodeBase64(it.type),
                        )
                    },
                sortOrder = command.sortOrder,
                status = SaveTaskStatus.PENDING,
                createdDate = now,
                modifiedDate = now,
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

        val task =
            writeTransactionTemplate.execute {
                val task =
                    findSaveTaskOutput.findByIdForUpdate(taskIdentifier)
                        ?: return@execute null

                if (task.status != SaveTaskStatus.PENDING) {
                    logger.warn("Task is not pending: {}", taskIdentifier)
                    return@execute null
                }

                task.start()

                saveSaveTaskOutput.save(task)
            } ?: return

        try {
            save(task)

            writeTransactionTemplate.execute {
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
        val decodedPath = Paths.get(fileProperty.unsortedPath, decodeBase64(task.path)).toString()

        val fileHashValue = hashFileOutput.calculateFileHash(decodedPath)

        val identifier = UUID.randomUUID()
        val extension = decodedPath.substringAfterLast('.').lowercase()
        val newRelativePath = "${fileProperty.filesPath}/non_group/${UUID.randomUUID()}.$extension"

        val saveResult =
            writeTransactionTemplate.execute {
                if (findFileHashOutput.existByHash(fileHashValue)) {
                    throw RuntimeException("File already exists with hash: $fileHashValue")
                }

                val now = OffsetDateTime.now()
                val file =
                    File(
                        identifier = identifier,
                        fileGroupIdentifier = task.fileGroupIdentifier,
                        name = task.name,
                        type = task.type,
                        path = encodeBase64(newRelativePath),
                        thumbnail =
                            createThumbnailOutput.createThumbnail(
                                decodedPath,
                            ),
                        sortOrder = task.sortOrder,
                        createdDate = now,
                        modifiedDate = now,
                        lastViewDate = null,
                    )

                val fileHash =
                    FileHash(
                        identifier = UUID.randomUUID(),
                        fileIdentifier = file.identifier,
                        hash = fileHashValue,
                    )

                val tagByNameAndType =
                    findTagOutput.findAllByNameInAndTypeIn(
                        names = task.tags.map { it.name },
                        types = task.tags.map { it.type },
                    ).associateBy { it.name to it.type }

                val newTags =
                    task.tags
                        .filterNot { tagByNameAndType.containsKey(Pair(it.name, it.type)) }
                        .map {
                            Tag(
                                identifier = UUID.randomUUID(),
                                name = it.name,
                                type = it.type,
                            )
                        }

                val totalTags: List<Tag> = tagByNameAndType.values + newTags

                val fileTags =
                    totalTags.map {
                        FileTag(
                            identifier = UUID.randomUUID(),
                            fileIdentifier = file.identifier,
                            tagIdentifier = it.identifier,
                        )
                    }

                deleteUnsortedFileOutput.deleteByPath(task.path)
                saveFileOutput.save(file)
                saveTagOutput.saveAll(newTags)
                saveFileTagOutput.saveAll(fileTags)
                saveFileHashOutput.save(fileHash)

                // 보상 트랜잭션을 위한 정보 반환
                Triple(file, newTags, fileHash)
            }!!

        val (file, newTags, fileHash) = saveResult

        try {
            moveFileOutput.moveFile(
                sourcePath = decodedPath,
                destinationPath = newRelativePath,
            )
        } catch (e: Exception) {
            logger.error("Failed to move file from {} to {}, performing compensation transaction", decodedPath, newRelativePath, e)

            // 보상 트랜잭션 실행 - 저장된 데이터들을 역순으로 삭제
            writeTransactionTemplate.execute {
                try {
                    deleteFileHashOutput.deleteByFileIdentifier(file.identifier)
                    deleteFileTagOutput.deleteByFileIdentifier(file.identifier)
                    deleteTagOutput.deleteByIdentifiers(newTags.map { it.identifier })
                    deleteFileEntityOutput.deleteByIdentifier(file.identifier)
                    logger.info("Compensation transaction completed successfully for file: {}", file.identifier)
                } catch (compensationException: Exception) {
                    logger.error("Failed to execute compensation transaction for file: {}", file.identifier, compensationException)
                    throw RuntimeException("File move failed and compensation transaction also failed", compensationException)
                }
            }

            throw RuntimeException("Failed to move file: ${e.message}", e)
        }
    }
}
