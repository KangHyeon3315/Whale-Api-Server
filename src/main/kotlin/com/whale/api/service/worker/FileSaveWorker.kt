package com.whale.api.service.worker

import com.fasterxml.jackson.databind.ObjectMapper
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
        
        if (!Files.exists(sourcePath)) {
            throw RuntimeException("Source file not found: ${payload.path}")
        }
        
        // 파일 그룹 생성 (단일 파일용)
        val fileGroupIdentifier = UUID.randomUUID()
        val now = OffsetDateTime.now()
        
        val fileGroup = FileGroupEntity(
            identifier = fileGroupIdentifier,
            name = payload.name,
            type = payload.type,
            thumbnail = null,
            createdDate = now,
            modifiedDate = now
        )
        
        fileGroupRepository.save(fileGroup)
        
        // 파일을 새 위치로 이동
        val extension = sourcePath.toString().substringAfterLast('.').lowercase()
        val newPath = Paths.get(mediaFileProperty.filesPath, "single", "${payload.fileIdentifier}.$extension")
        val newFullPath = basePath.resolve(newPath)
        
        Files.createDirectories(newFullPath.parent)
        Files.move(sourcePath, newFullPath)
        
        // 파일 엔티티 생성
        val fileEntity = FileEntity(
            identifier = payload.fileIdentifier,
            fileGroup = fileGroup,
            name = payload.name,
            type = payload.type,
            path = newPath.toString(),
            thumbnail = null,
            sortOrder = 0,
            createdDate = now,
            modifiedDate = now,
            lastViewDate = null
        )
        
        fileRepository.save(fileEntity)
        
        // 파일 해시 계산 (이미지 파일만)
        if (extension in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }) {
            try {
                val hash = calculateFileHash(newFullPath)
                val fileHashEntity = FileHashEntity(
                    identifier = UUID.randomUUID(),
                    file = fileEntity,
                    hash = hash
                )
                fileHashRepository.save(fileHashEntity)
            } catch (e: Exception) {
                logger.warn("Failed to calculate hash for file: $newFullPath", e)
            }
        }
        
        // 태그 처리
        payload.tags.forEach { tagDto ->
            val tag = tagRepository.findByName(tagDto.name) ?: run {
                val newTag = TagEntity(
                    identifier = UUID.randomUUID(),
                    name = tagDto.name,
                    type = tagDto.type
                )
                tagRepository.save(newTag)
                newTag
            }
            
            val fileTag = FileTagEntity(
                identifier = UUID.randomUUID(),
                file = fileEntity,
                tag = tag
            )
            fileTagRepository.save(fileTag)
        }
        
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
