package com.whale.api.archive.adapter.input.web

import com.whale.api.archive.application.port.`in`.GetArchiveFileUseCase
import com.whale.api.global.annotation.RequireAuth
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.util.UUID
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/archives")
class ArchiveFileWebController(
    private val getArchiveFileUseCase: GetArchiveFileUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @RequireAuth
    @GetMapping("/items/{itemId}/file")
    fun getArchiveFile(
        @PathVariable itemId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<StreamingResponseBody> {
        val rangeHeader = request.getHeader("Range")
        val userAgent = request.getHeader("User-Agent")

        logger.info { "Getting archive file: itemId=$itemId, range=$rangeHeader, userAgent=$userAgent" }

        val fileResource = getArchiveFileUseCase.getArchiveFile(itemId, rangeHeader)

        logger.info { "Serving archive file: ${fileResource.fileName} (${fileResource.size} bytes), mimeType=${fileResource.mimeType}, isPartial=${fileResource.isPartialContent()}" }

        val streamingResponseBody = StreamingResponseBody { outputStream ->
            generateFileStream(fileResource, outputStream)
        }

        val responseBuilder = if (fileResource.isPartialContent()) {
            ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE,
                    "bytes ${fileResource.rangeStart}-${fileResource.rangeEnd}/${fileResource.size}")
        } else {
            ResponseEntity.ok()
        }

        return responseBuilder
            .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, fileResource.contentLength.toString())
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${fileResource.fileName}\"")
            .body(streamingResponseBody)
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/thumbnail")
    fun getArchiveFileThumbnail(
        @PathVariable itemId: UUID,
    ): ResponseEntity<StreamingResponseBody> {
        logger.info { "Getting archive file thumbnail: itemId=$itemId" }

        val fileResource = getArchiveFileUseCase.getArchiveFileThumbnail(itemId)

        logger.info { "Serving thumbnail: ${fileResource.fileName} (${fileResource.size} bytes), mimeType=${fileResource.mimeType}" }

        val streamingResponseBody = StreamingResponseBody { outputStream ->
            fileResource.inputStream.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, fileResource.size.toString())
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400") // 24시간 캐시
            .body(streamingResponseBody)
    }

    @RequireAuth
    @GetMapping("/items/{itemId}/live-photo-video")
    fun getLivePhotoVideo(
        @PathVariable itemId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<StreamingResponseBody> {
        val rangeHeader = request.getHeader("Range")
        val userAgent = request.getHeader("User-Agent")

        logger.info { "Getting live photo video: itemId=$itemId, range=$rangeHeader, userAgent=$userAgent" }

        val fileResource = getArchiveFileUseCase.getLivePhotoVideo(itemId, rangeHeader)

        logger.info { "Serving live photo video: ${fileResource.fileName} (${fileResource.size} bytes), mimeType=${fileResource.mimeType}, isPartial=${fileResource.isPartialContent()}" }

        val streamingResponseBody = StreamingResponseBody { outputStream ->
            generateFileStream(fileResource, outputStream)
        }

        val responseBuilder = if (fileResource.isPartialContent()) {
            ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE,
                    "bytes ${fileResource.rangeStart}-${fileResource.rangeEnd}/${fileResource.size}")
        } else {
            ResponseEntity.ok()
        }

        return responseBuilder
            .header(HttpHeaders.CONTENT_TYPE, fileResource.mimeType)
            .header(HttpHeaders.CONTENT_LENGTH, fileResource.contentLength.toString())
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${fileResource.fileName}\"")
            .body(streamingResponseBody)
    }

    private fun generateFileStream(fileResource: com.whale.api.archive.domain.ArchiveFileResource, outputStream: OutputStream) {
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
}
