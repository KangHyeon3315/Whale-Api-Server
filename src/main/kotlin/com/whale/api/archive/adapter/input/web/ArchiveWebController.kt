package com.whale.api.archive.adapter.input.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whale.api.archive.adapter.input.web.request.CreateArchiveRequest
import com.whale.api.archive.adapter.input.web.response.ArchiveItemPageResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveItemResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveMetadataResponse
import com.whale.api.archive.adapter.input.web.response.ArchiveResponse
import com.whale.api.archive.application.port.`in`.CreateArchiveUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemContentUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveItemsUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveStatusUseCase
import com.whale.api.archive.application.port.`in`.UploadArchiveItemUseCase
import com.whale.api.archive.application.port.`in`.command.UploadArchiveItemCommand
import com.whale.api.global.annotation.RequireAuth
import jakarta.servlet.http.HttpServletRequest
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
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
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

    /**
     * 스트리밍 방식 파일 업로드 (메모리 효율적)
     * Servlet Part API를 사용하여 대용량 파일을 메모리에 전체 로드하지 않고 스트리밍으로 처리합니다.
     */
    @RequireAuth
    @PostMapping("/{archiveId}/items/streaming")
    fun uploadItemStreaming(
        @PathVariable archiveId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<ArchiveItemResponse> {
        logger.info { "Streaming upload to archive: $archiveId" }

        // Servlet Part API를 사용하여 스트리밍 처리
        val multipartRequest =
            request as? StandardMultipartHttpServletRequest
                ?: throw IllegalArgumentException("Request must be multipart")

        val filePart =
            multipartRequest.getFile("file")
                ?: throw IllegalArgumentException("File part is required")

        val livePhotoVideoPart = multipartRequest.getFile("livePhotoVideo")

        val originalPath =
            request.getParameter("originalPath")
                ?: throw IllegalArgumentException("originalPath is required")

        val isLivePhoto = request.getParameter("isLivePhoto")?.toBoolean() ?: false

        val originalCreatedDate =
            request.getParameter("originalCreatedDate")?.let {
                OffsetDateTime.parse(it)
            }

        val originalModifiedDate =
            request.getParameter("originalModifiedDate")?.let {
                OffsetDateTime.parse(it)
            }

        val metadataJson = request.getParameter("metadata")

        logger.info {
            "Streaming upload: file=${filePart.originalFilename} (${filePart.size} bytes), " +
                "originalPath=$originalPath, isLivePhoto=$isLivePhoto"
        }

        // JSON 문자열을 Map으로 파싱
        val metadata =
            try {
                if (metadataJson.isNullOrBlank()) {
                    emptyMap()
                } else {
                    objectMapper.readValue<Map<String, String>>(metadataJson)
                }
            } catch (e: Exception) {
                logger.warn { "Failed to parse metadata JSON: $metadataJson" }
                emptyMap<String, String>()
            }

        val command =
            UploadArchiveItemCommand(
                archiveIdentifier = archiveId,
                file = filePart,
                originalPath = originalPath,
                isLivePhoto = isLivePhoto,
                livePhotoVideo = livePhotoVideoPart,
                originalCreatedDate = originalCreatedDate,
                originalModifiedDate = originalModifiedDate,
                metadata = metadata,
            )

        val archiveItem = uploadArchiveItemUseCase.uploadItem(command)
        return ResponseEntity.ok(ArchiveItemResponse.from(archiveItem))
    }

    /**
     * 기존 MultipartFile 방식 업로드 (하위 호환성 유지)
     * 작은 파일의 경우 이 방식을 사용할 수 있습니다.
     */
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
    ): ResponseEntity<ArchiveItemResponse> {
        logger.info {
            "Uploading item to archive: $archiveId, " +
                "file: ${file.originalFilename} (${file.size} bytes), " +
                "originalPath: $originalPath, " +
                "isLivePhoto: $isLivePhoto, " +
                "hasLivePhotoVideo: ${livePhotoVideo != null}, " +
                "metadata: $metadataJson"
        }

        // JSON 문자열을 Map/List로 파싱
        val metadata =
            try {
                if (metadataJson.isNullOrBlank()) {
                    emptyMap()
                } else {
                    objectMapper.readValue<Map<String, String>>(metadataJson)
                }
            } catch (e: Exception) {
                logger.warn { "Failed to parse metadata JSON: $metadataJson" }
                emptyMap<String, String>()
            }

        logger.info { "Parsed metadata: $metadata" }

        val command =
            UploadArchiveItemCommand(
                archiveIdentifier = archiveId,
                file = file,
                originalPath = originalPath,
                isLivePhoto = isLivePhoto,
                livePhotoVideo = livePhotoVideo,
                originalCreatedDate = originalCreatedDate,
                originalModifiedDate = originalModifiedDate,
                metadata = metadata,
            )

        val archiveItem = uploadArchiveItemUseCase.uploadItem(command)
        return ResponseEntity.ok(ArchiveItemResponse.from(archiveItem))
    }

    @RequireAuth
    @GetMapping("/{archiveId}/items")
    fun getArchiveItems(
        @PathVariable archiveId: UUID,
        @RequestParam("fileName", required = false) fileName: String?,
        @RequestParam("cursor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) cursor: OffsetDateTime?,
        @RequestParam("limit", required = false, defaultValue = "20") limit: Int,
    ): ResponseEntity<ArchiveItemPageResponse> {
        logger.info { "Getting archive items: archiveId=$archiveId, fileName=$fileName, cursor=$cursor, limit=$limit" }

        val page = getArchiveItemsUseCase.getArchiveItems(archiveId, fileName, null, cursor, limit)

        logger.info {
            "Retrieved ${page.items.size} items for archive: $archiveId " +
                "(hasNext=${page.hasNext}, totalCount=${page.totalCount})"
        }
        return ResponseEntity.ok(ArchiveItemPageResponse.from(page.items, page.hasNext, page.totalCount))
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
        val categories =
            items.groupBy { it.getFileCategory() }
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
}
