package com.whale.api.file.adapter.input.web

import com.whale.api.file.adapter.input.web.request.DeleteFileRequest
import com.whale.api.file.adapter.input.web.request.SaveFileRequest
import com.whale.api.file.adapter.input.web.response.FileTreeItemDto
import com.whale.api.file.adapter.input.web.response.UnsortedTreeResponse
import com.whale.api.file.application.port.`in`.DeleteFileUseCase
import com.whale.api.file.application.port.`in`.GetAllTagsUseCase
import com.whale.api.file.application.port.`in`.GetFileTypesUseCase
import com.whale.api.file.application.port.`in`.GetFileUseCase
import com.whale.api.file.application.port.`in`.GetThumbnailUseCase
import com.whale.api.file.application.port.`in`.GetUnsortedTreeUseCase
import com.whale.api.file.application.port.`in`.SaveFileUseCase
import com.whale.api.file.application.port.`in`.SortType
import com.whale.api.file.domain.FileResource
import com.whale.api.file.domain.property.FileProperty
import com.whale.api.global.annotation.RequireAuth
import com.whale.api.global.utils.Encoder.decodeBase64
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream

@RestController
@RequestMapping("/files")
class FileWebController(
    private val saveFileUseCase: SaveFileUseCase,
    private val getFileUseCase: GetFileUseCase,
    private val getThumbnailUseCase: GetThumbnailUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val getFileTypesUseCase: GetFileTypesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getUnsortedTreeUseCase: GetUnsortedTreeUseCase,
    private val fileProperty: FileProperty,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @PostMapping("/save-request")
    fun requestSave(
        @RequestBody request: SaveFileRequest,
    ): ResponseEntity<Void> {
        saveFileUseCase.requestSave(request.toCommand())
        return ResponseEntity.ok().build()
    }

    @RequireAuth
    @GetMapping("/unsorted/image")
    fun getImage(
        @RequestParam path: String,
    ): ResponseEntity<StreamingResponseBody> {
        val fileResource = getFileUseCase.getUnsortedImage(path)

        val streamingResponseBody =
            StreamingResponseBody { outputStream ->
                fileResource.inputStream.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, fileResource.size.toString())
            .body(streamingResponseBody)
    }

    @GetMapping("/unsorted/video")
    fun getVideo(
        @RequestParam path: String,
        request: HttpServletRequest,
    ): ResponseEntity<StreamingResponseBody> {
        // HEAD 요청인 경우 파일 메타데이터만 확인하고 빠르게 응답
        if (request.method == "HEAD") {
            return handleVideoHead(path)
        }

        val rangeHeader = request.getHeader("Range")
        val fileResource = getFileUseCase.getVideo(path, rangeHeader)

        logger.debug("get_video path: $path, range_header: $rangeHeader (${fileResource.size} bytes)")

        val streamingResponseBody =
            StreamingResponseBody { outputStream ->
                generateVideoStream(fileResource, outputStream)
            }

        return if (fileResource.isPartialContent()) {
            ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
                .header(HttpHeaders.CONTENT_RANGE, "bytes ${fileResource.rangeStart}-${fileResource.rangeEnd}/${fileResource.size}")
                .header(HttpHeaders.CONTENT_LENGTH, fileResource.contentLength.toString())
                .header("Total-Content-Length", fileResource.size.toString())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(streamingResponseBody)
        } else {
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
                .header(HttpHeaders.CONTENT_LENGTH, fileResource.size.toString())
                .header("Total-Content-Length", fileResource.size.toString())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(streamingResponseBody)
        }
    }

    private fun handleVideoHead(path: String): ResponseEntity<StreamingResponseBody> {
        // HEAD 요청을 위한 빠른 파일 정보 확인
        val normalizedPath =
            java.nio.file.Paths.get(
                fileProperty.basePath,
                fileProperty.unsortedPath,
                path.replace(" ", "+"),
            ).toString()

        val file = java.io.File(normalizedPath)
        if (!file.exists()) {
            throw RuntimeException("File not found: $path")
        }

        val extension = file.extension.lowercase()
        val mimeType = fileProperty.mimeTypeMapping[".$extension"] ?: "application/octet-stream"

        // 빈 StreamingResponseBody (HEAD 요청은 본문이 없음)
        val emptyStreamingResponseBody = StreamingResponseBody { }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, file.length().toString())
            .header("Total-Content-Length", file.length().toString())
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .body(emptyStreamingResponseBody)
    }

    private fun generateVideoStream(
        fileResource: FileResource,
        outputStream: OutputStream,
    ) {
        fileResource.inputStream.use { inputStream ->
            if (fileResource.isRangeRequest) {
                val buffer = ByteArray(8192)
                var bytesToRead = fileResource.contentLength

                while (bytesToRead > 0) {
                    val bytesRead = inputStream.read(buffer, 0, minOf(buffer.size.toLong(), bytesToRead).toInt())
                    if (bytesRead == -1) break
                    outputStream.write(buffer, 0, bytesRead)
                    bytesToRead -= bytesRead
                }
            } else {
                inputStream.copyTo(outputStream)
            }
        }
    }

    @RequireAuth
    @DeleteMapping("/unsorted")
    fun deleteFileByPath(
        @RequestBody request: DeleteFileRequest,
    ): ResponseEntity<String> {
        deleteFileUseCase.deleteUnsortedFileByPath(request.path)
        return ResponseEntity.ok("OK")
    }

    @RequireAuth
    @GetMapping("/types")
    fun findAllTypes(): ResponseEntity<Map<String, List<String>>> {
        val types = getFileTypesUseCase.getAllFileTypes()
        return ResponseEntity.ok(mapOf("types" to types.map { decodeBase64(it) }))
    }

    @RequireAuth
    @GetMapping("/tags")
    fun findAllTags(): ResponseEntity<Map<String, List<Map<String, String>>>> {
        val tags = getAllTagsUseCase.getAllTags()
        val tagDtos =
            tags.map { tag ->
                mapOf(
                    "identifier" to tag.identifier.toString(),
                    "name" to decodeBase64(tag.name),
                    "type" to decodeBase64(tag.type),
                )
            }
        return ResponseEntity.ok(mapOf("tags" to tagDtos))
    }

    @RequireAuth
    @GetMapping("/unsorted/tree")
    fun getUnsortedTree(
        @RequestParam(defaultValue = "") path: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "name") sort: String,
    ): ResponseEntity<UnsortedTreeResponse> {
        val sortType =
            when (sort.lowercase()) {
                "number" -> SortType.NUMBER
                else -> SortType.NAME
            }

        val fileTreeItems = getUnsortedTreeUseCase.getUnsortedTree(path, cursor, limit, sortType)

        val fileTreeItemDtos =
            fileTreeItems.map { item ->
                FileTreeItemDto(
                    name = item.name,
                    isDir = item.isDir,
                    extension = item.extension,
                )
            }

        return ResponseEntity.ok(UnsortedTreeResponse(files = fileTreeItemDtos))
    }

    @RequireAuth
    @GetMapping("/unsorted/thumbnail")
    fun getThumbnail(
        @RequestParam path: String,
    ): ResponseEntity<StreamingResponseBody> {
        logger.debug("Getting thumbnail for path: $path")

        val fileResource = getThumbnailUseCase.getUnsortedThumbnail(path)

        val streamingResponseBody =
            StreamingResponseBody { outputStream ->
                fileResource.inputStream.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, fileResource.size.toString())
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
            .body(streamingResponseBody)
    }
}
