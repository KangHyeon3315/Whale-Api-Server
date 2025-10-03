package com.whale.api.service.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.whale.api.file.adapter.output.persistence.repository.FileGroupRepository
import com.whale.api.file.adapter.output.persistence.repository.FileHashRepository
import com.whale.api.file.adapter.output.persistence.repository.FileRepository
import com.whale.api.file.adapter.output.persistence.repository.FileTagRepository
import com.whale.api.file.adapter.output.persistence.repository.TagRepository
import com.whale.api.file.adapter.output.persistence.repository.UnsortedFileRepository
import com.whale.api.global.config.MediaFileProperty
import com.whale.api.model.file.*
import com.whale.api.model.taskqueue.TaskQueueEntity
import com.whale.api.model.taskqueue.dto.FileSaveTaskPayload
import com.whale.api.repository.file.*
import com.whale.api.service.taskqueue.TaskQueueService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.*

@Service
class FileSaveWorker(
    private val taskQueueService: TaskQueueService,
    private val fileRepository: FileRepository,
    private val fileGroupRepository: FileGroupRepository,
    private val tagRepository: TagRepository,
    private val fileTagRepository: FileTagRepository,
    private val fileHashRepository: FileHashRepository,
    private val unsortedFileRepository: UnsortedFileRepository,
    private val mediaFileProperty: MediaFileProperty,
    private val objectMapper: ObjectMapper,
    private val writeTransactionTemplate: TransactionTemplate,
) {

    private val logger = KotlinLogging.logger {}

    fun processFileSaveTask(task: TaskQueueEntity): Boolean {
        return try {
            logger.info("Processing file save task: ${task.identifier}")

            val payload = objectMapper.readValue(task.payload, FileSaveTaskPayload::class.java)

            writeTransactionTemplate.execute {
                processFileSave(payload)
            }

            taskQueueService.markAsCompleted(task.identifier)
            logger.info("File save task completed: ${task.identifier}")
            true
        } catch (e: Exception) {
            logger.error("Failed to process file save task: ${task.identifier}", e)
            taskQueueService.markAsFailed(task.identifier, e.message ?: "Unknown error")
            false
        }
    }

    private fun processFileSave(payload: FileSaveTaskPayload) {
        val basePath = Paths.get(payload.basePath)
        val sourcePath = basePath.resolve(payload.path)

        // 파일 경로 유효성 검증
        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            throw RuntimeException("File not found: ${payload.path}")
        }

        // 파일 해시 계산 및 중복 검사 (모든 파일에 대해)
        val hash = calculateFileHash(sourcePath)
        val existingHash = fileHashRepository.findByHash(hash)
        if (existingHash != null) {
            throw RuntimeException("File already exists with hash: $hash")
        }

        // 파일을 새 위치로 이동 (non_group 디렉토리 사용)
        val extension = sourcePath.toString().substringAfterLast('.').lowercase()
        val newPath = Paths.get(mediaFileProperty.filesPath, "non_group", "${payload.fileIdentifier}.$extension")
        val newFullPath = basePath.resolve(newPath)

        Files.createDirectories(newFullPath.parent)
        Files.move(sourcePath, newFullPath)

        val now = OffsetDateTime.now()

        // 파일 엔티티 생성 (파일 그룹 없이)
        val fileEntity = FileEntity(
            identifier = payload.fileIdentifier,
            fileGroup = null, // 단일 파일은 그룹 없음
            name = payload.name,
            type = payload.type,
            path = newPath.toString(),
            thumbnail = null,
            sortOrder = null, // 단일 파일은 정렬 순서 없음
            createdDate = now,
            modifiedDate = now,
            lastViewDate = null
        )

        fileRepository.save(fileEntity)

        // 파일 해시 저장
        val fileHashEntity = FileHashEntity(
            identifier = UUID.randomUUID(),
            file = fileEntity,
            hash = hash
        )
        fileHashRepository.save(fileHashEntity)

        // 태그 처리 - 존재하지 않는 태그는 새로 생성
        val existingTags = tagRepository.findByNameAndTypeIn(
            payload.tags.map { it.name to it.type }
        )
        val existingTagMap = existingTags.associateBy { it.name to it.type }

        val allTags = mutableListOf<TagEntity>()

        payload.tags.forEach { tagDto ->
            val tagKey = tagDto.name to tagDto.type
            val tag = existingTagMap[tagKey] ?: run {
                val newTag = TagEntity(
                    identifier = UUID.randomUUID(),
                    name = tagDto.name,
                    type = tagDto.type
                )
                tagRepository.save(newTag)
                newTag
            }
            allTags.add(tag)
        }

        // 파일-태그 연결
        val fileTags = allTags.map { tag ->
            FileTagEntity(
                identifier = UUID.randomUUID(),
                file = fileEntity,
                tag = tag
            )
        }
        fileTagRepository.saveAll(fileTags)

        // unsorted file 제거 (해당하는 경우)
        unsortedFileRepository.deleteByPath(payload.path)

        logger.info("File saved successfully: ${payload.fileIdentifier}")
    }

    private fun calculateFileHash(filePath: java.nio.file.Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(filePath).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
