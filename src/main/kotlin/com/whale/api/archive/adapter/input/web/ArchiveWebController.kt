package com.whale.api.archive.adapter.input.web

import com.whale.api.archive.adapter.input.web.request.CreateArchiveRequest
import com.whale.api.archive.adapter.input.web.response.ArchiveItemResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveMetadataResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveResponse
import com.whale.api.archive.application.port.`in`.CreateArchiveUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemContentUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemsUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveStatusUseCase
import com.whale.api.archive.application.port.`in`.StartArchiveUseCase
import com.whale.api.archive.application.port.`in`.UploadArchiveItemUseCase
import com.whale.api.archive.application.port.`in`.command.UploadArchiveItemCommand
import com.whale.api.global.annotation.RequireAuth
import mu.KotlinLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/archives")
class ArchiveWebController(
    private val createArchiveUseCase: CreateArchiveUseCase,
    private val startArchiveUseCase: StartArchiveUseCase,
    private val getArchiveStatusUseCase: GetArchiveStatusUseCase,
    private val uploadArchiveItemUseCase: UploadArchiveItemUseCase,
    private val getArchiveItemsUseCase: GetArchiveItemsUseCase,
    private val getArchiveItemContentUseCase: GetArchiveItemContentUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @PostMapping
    fun createArchive(
        @RequestBody request: CreateArchiveRequest,
    ): ResponseEntity<ArchiveResponse> {
        logger.info { "Creating archive: ${request.name}" }
        val archive = createArchiveUseCase.createArchive(request.toCommand())
        return ResponseEntity.ok(ArchiveResponse.from(archive))
    }

    @RequireAuth
    @PostMapping("/{archiveId}/start")
    fun startArchive(
        @PathVariable archiveId: UUID,
    ): ResponseEntity<Void> {
        logger.info { "Starting archive: $archiveId" }
        startArchiveUseCase.startArchive(archiveId)
        return ResponseEntity.ok().build()
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
        @RequestParam("metadata", required = false) metadata: Map<String, String>?,
    ): ResponseEntity<ArchiveItemResponse> {
        logger.info { "Uploading item to archive: $archiveId" }

        val command = UploadArchiveItemCommand(
            archiveIdentifier = archiveId,
            file = file,
            originalPath = originalPath,
            isLivePhoto = isLivePhoto,
            livePhotoVideo = livePhotoVideo,
            originalCreatedDate = originalCreatedDate,
            originalModifiedDate = originalModifiedDate,
            metadata = metadata ?: emptyMap(),
        )

        val archiveItem = uploadArchiveItemUseCase.uploadItem(command)
        return ResponseEntity.ok(ArchiveItemResponse.from(archiveItem))
    }

    @RequireAuth
    @GetMapping("/{archiveId}/items")
    fun getArchiveItems(
        @PathVariable archiveId: UUID,
    ): ResponseEntity<List<ArchiveItemResponse>> {
        val items = getArchiveItemsUseCase.getArchiveItems(archiveId)
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
        val items = getArchiveItemsUseCase.getArchiveItems(archiveId)
        val filteredItems = items.filter { it.getFileCategory() == category }
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
        val content = getArchiveItemContentUseCase.getTextContent(itemId)
        return ResponseEntity.ok(mapOf("content" to content))
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/content/preview")
    fun getTextContentPreview(
        @PathVariable itemId: UUID,
        @RequestParam("maxLength", defaultValue = "1000") maxLength: Int,
    ): ResponseEntity<Map<String, String>> {
        val content = getArchiveItemContentUseCase.getTextContentPreview(itemId, maxLength)
        return ResponseEntity.ok(mapOf("content" to content, "isPreview" to "true"))
    }
}
