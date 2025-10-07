package com.whale.api.archive.adapter.input.web

import com.whale.api.archive.adapter.input.web.request.CreateArchiveRequest
import com.whale.api.archive.adapter.input.web.response.ArchiveItemResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveMetadataResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveResponse
import com.whale.api.archive.application.port.`in`.CreateArchiveUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemContentUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemsUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveTagsUseCase
import com.whale.api.archive.application.port.`in`.ManageArchiveTagsUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveStatusUseCase

import com.whale.api.archive.application.port.`in`.UploadArchiveItemUseCase
import com.whale.api.archive.application.port.`in`.command.UploadArchiveItemCommand
import com.whale.api.global.annotation.RequireAuth
import mu.KotlinLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/archives")
class ArchiveWebController(
    private val createArchiveUseCase: CreateArchiveUseCase,
    private val getArchiveStatusUseCase: GetArchiveStatusUseCase,
    private val uploadArchiveItemUseCase: UploadArchiveItemUseCase,
    private val getArchiveItemsUseCase: GetArchiveItemsUseCase,
    private val getArchiveItemContentUseCase: GetArchiveItemContentUseCase,
    private val getArchiveTagsUseCase: GetArchiveTagsUseCase,
    private val manageArchiveTagsUseCase: ManageArchiveTagsUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @PostMapping
    fun createArchive(
        @RequestBody request: CreateArchiveRequest,
    ): ResponseEntity<ArchiveResponse> {
        logger.info {
            "Creating archive: name='${request.name}', " +
            "description='${request.description}', " +
            "totalItems=${request.totalItems}"
        }
        val archive = createArchiveUseCase.createArchive(request.toCommand())
        return ResponseEntity.ok(ArchiveResponse.from(archive))
    }

    @RequireAuth
    @GetMapping
    fun getAllArchives(): ResponseEntity<List<ArchiveResponse>> {
        val archives = getArchiveStatusUseCase.getAllArchives()
        return ResponseEntity.ok(archives.map { ArchiveResponse.from(it) })
    }

    @RequireAuth
    @GetMapping("/{archiveId}")
    fun getArchive(
        @PathVariable archiveId: UUID,
    ): ResponseEntity<ArchiveResponse> {
        val archive = getArchiveStatusUseCase.getArchive(archiveId)
        return ResponseEntity.ok(ArchiveResponse.from(archive))
    }

    @RequireAuth
    @PostMapping("/{archiveId}/items")
    fun uploadItem(
        @PathVariable archiveId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("originalPath") originalPath: String,
        @RequestParam("isLivePhoto", defaultValue = "false") isLivePhoto: Boolean,
        @RequestParam("livePhotoVideo", required = false) livePhotoVideo: MultipartFile?,
        @RequestParam("originalCreatedDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        originalCreatedDate: OffsetDateTime?,
        @RequestParam("originalModifiedDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        originalModifiedDate: OffsetDateTime?,
        @RequestParam("metadata", required = false) metadataJson: String?,
        @RequestParam("tags", required = false) tagsJson: String?,
    ): ResponseEntity<ArchiveItemResponse> {
        logger.info {
            "Uploading item to archive: $archiveId, " +
            "file: ${file.originalFilename} (${file.size} bytes), " +
            "originalPath: $originalPath, " +
            "isLivePhoto: $isLivePhoto, " +
            "hasLivePhotoVideo: ${livePhotoVideo != null}, " +
            "metadata: $metadataJson, " +
            "tags: $tagsJson"
        }

        // JSON 문자열을 Map/List로 파싱
        val metadata = try {
            if (metadataJson.isNullOrBlank()) emptyMap()
            else objectMapper.readValue<Map<String, String>>(metadataJson)
        } catch (e: Exception) {
            logger.warn { "Failed to parse metadata JSON: $metadataJson" }
            emptyMap<String, String>()
        }

        val tags = try {
            if (tagsJson.isNullOrBlank()) emptyList()
            else objectMapper.readValue<List<String>>(tagsJson)
        } catch (e: Exception) {
            logger.warn { "Failed to parse tags JSON: $tagsJson" }
            emptyList<String>()
        }

        logger.info { "Parsed metadata: $metadata, tags: $tags" }

        val command = UploadArchiveItemCommand(
            archiveIdentifier = archiveId,
            file = file,
            originalPath = originalPath,
            isLivePhoto = isLivePhoto,
            livePhotoVideo = livePhotoVideo,
            originalCreatedDate = originalCreatedDate,
            originalModifiedDate = originalModifiedDate,
            metadata = metadata,
            tags = tags,
        )

        val archiveItem = uploadArchiveItemUseCase.uploadItem(command)
        return ResponseEntity.ok(ArchiveItemResponse.from(archiveItem))
    }

    @RequireAuth
    @GetMapping("/{archiveId}/items")
    fun getArchiveItems(
        @PathVariable archiveId: UUID,
    ): ResponseEntity<List<ArchiveItemResponse>> {
        logger.info { "Getting archive items: archiveId=$archiveId" }
        val items = getArchiveItemsUseCase.getArchiveItems(archiveId)
        logger.info { "Retrieved ${items.size} items for archive: $archiveId" }
        return ResponseEntity.ok(items.map { ArchiveItemResponse.from(it) })
    }

    @RequireAuth
    @GetMapping("/items/{itemId}")
    fun getArchiveItem(
        @PathVariable itemId: UUID,
    ): ResponseEntity<ArchiveItemResponse> {
        val item = getArchiveItemsUseCase.getArchiveItem(itemId)
        return ResponseEntity.ok(ArchiveItemResponse.from(item))
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/metadata")
    fun getArchiveItemMetadata(
        @PathVariable itemId: UUID,
    ): ResponseEntity<List<ArchiveMetadataResponse>> {
        val metadata = getArchiveItemsUseCase.getArchiveItemMetadata(itemId)
        return ResponseEntity.ok(metadata.map { ArchiveMetadataResponse.from(it) })
    }

    @RequireAuth
    @GetMapping("/{archiveId}/items/category/{category}")
    fun getArchiveItemsByCategory(
        @PathVariable archiveId: UUID,
        @PathVariable category: String,
    ): ResponseEntity<List<ArchiveItemResponse>> {
        logger.info { "Getting archive items by category: archiveId=$archiveId, category=$category" }
        val items = getArchiveItemsUseCase.getArchiveItems(archiveId)
        val filteredItems = items.filter { it.getFileCategory() == category }
        logger.info { "Retrieved ${filteredItems.size} items for category '$category' in archive: $archiveId" }
        return ResponseEntity.ok(filteredItems.map { ArchiveItemResponse.from(it) })
    }

    @RequireAuth
    @GetMapping("/{archiveId}/categories")
    fun getArchiveCategories(
        @PathVariable archiveId: UUID,
    ): ResponseEntity<Map<String, Int>> {
        val items = getArchiveItemsUseCase.getArchiveItems(archiveId)
        val categories = items.groupBy { it.getFileCategory() }
            .mapValues { it.value.size }
        return ResponseEntity.ok(categories)
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/content")
    fun getTextContent(
        @PathVariable itemId: UUID,
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Getting text content: itemId=$itemId" }
        val content = getArchiveItemContentUseCase.getTextContent(itemId)
        logger.info { "Serving text content: itemId=$itemId, contentLength=${content.length}" }
        return ResponseEntity.ok(mapOf("content" to content))
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/content/preview")
    fun getTextContentPreview(
        @PathVariable itemId: UUID,
        @RequestParam("maxLength", defaultValue = "1000") maxLength: Int,
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Getting text content preview: itemId=$itemId, maxLength=$maxLength" }
        val content = getArchiveItemContentUseCase.getTextContentPreview(itemId, maxLength)
        logger.info { "Serving text content preview: itemId=$itemId, contentLength=${content.length}, maxLength=$maxLength" }
        return ResponseEntity.ok(mapOf("content" to content, "isPreview" to "true"))
    }

    @RequireAuth
    @GetMapping("/tags")
    fun getAllTags(): ResponseEntity<List<Map<String, Any>>> {
        val tags = getArchiveTagsUseCase.getAllTags()
        val response = tags.map { tag ->
            mapOf(
                "identifier" to tag.identifier,
                "name" to tag.name,
                "type" to tag.type,
                "createdDate" to tag.createdDate
            )
        }
        return ResponseEntity.ok(response)
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/tags")
    fun getArchiveItemTags(
        @PathVariable itemId: UUID,
    ): ResponseEntity<List<Map<String, Any>>> {
        val tags = getArchiveTagsUseCase.getTagsByArchiveItem(itemId)
        val response = tags.map { tag ->
            mapOf(
                "identifier" to tag.identifier,
                "name" to tag.name,
                "type" to tag.type,
                "createdDate" to tag.createdDate
            )
        }
        return ResponseEntity.ok(response)
    }

    @RequireAuth
    @PostMapping("/items/{itemId}/tags")
    fun addTagsToArchiveItem(
        @PathVariable itemId: UUID,
        @RequestBody request: Map<String, List<String>>,
    ): ResponseEntity<List<Map<String, Any>>> {
        val tagNames = request["tags"] ?: throw IllegalArgumentException("Tags are required")
        val tags = manageArchiveTagsUseCase.addTagsToArchiveItem(itemId, tagNames)
        val response = tags.map { tag ->
            mapOf(
                "identifier" to tag.identifier,
                "name" to tag.name,
                "type" to tag.type,
                "createdDate" to tag.createdDate
            )
        }
        return ResponseEntity.ok(response)
    }

    @RequireAuth
    @PutMapping("/items/{itemId}/tags")
    fun updateArchiveItemTags(
        @PathVariable itemId: UUID,
        @RequestBody request: Map<String, List<String>>,
    ): ResponseEntity<List<Map<String, Any>>> {
        val tagNames = request["tags"] ?: emptyList()
        val tags = manageArchiveTagsUseCase.updateArchiveItemTags(itemId, tagNames)
        val response = tags.map { tag ->
            mapOf(
                "identifier" to tag.identifier,
                "name" to tag.name,
                "type" to tag.type,
                "createdDate" to tag.createdDate
            )
        }
        return ResponseEntity.ok(response)
    }

    @RequireAuth
    @DeleteMapping("/items/{itemId}/tags")
    fun removeTagsFromArchiveItem(
        @PathVariable itemId: UUID,
        @RequestBody request: Map<String, List<String>>,
    ): ResponseEntity<Void> {
        val tagNames = request["tags"] ?: throw IllegalArgumentException("Tags are required")
        manageArchiveTagsUseCase.removeTagsFromArchiveItem(itemId, tagNames)
        return ResponseEntity.ok().build()
    }

    @RequireAuth
    @GetMapping("/tags/{tagName}/items")
    fun getArchiveItemsByTag(
        @PathVariable tagName: String,
    ): ResponseEntity<List<UUID>> {
        val itemIds = getArchiveTagsUseCase.getArchiveItemsByTag(tagName)
        return ResponseEntity.ok(itemIds)
    }
}
