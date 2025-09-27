package com.whale.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "media.file")
data class MediaFileProperty(
    var basePath: String = "/tmp/whale-files",
    var thumbnailPath: String = "thumbnail",
    var unsortedPath: String = "unsorted",
    var filesPath: String = "files"
) {
    companion object {
        val VIDEO_EXTENSIONS = setOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v")
        val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".svg")
        
        val MIME_TYPE_MAPPING = mapOf(
            ".mp4" to "video/mp4",
            ".avi" to "video/x-msvideo",
            ".mkv" to "video/x-matroska",
            ".mov" to "video/quicktime",
            ".wmv" to "video/x-ms-wmv",
            ".flv" to "video/x-flv",
            ".webm" to "video/webm",
            ".m4v" to "video/x-m4v",
            ".jpg" to "image/jpeg",
            ".jpeg" to "image/jpeg",
            ".png" to "image/png",
            ".gif" to "image/gif",
            ".bmp" to "image/bmp",
            ".webp" to "image/webp",
            ".tiff" to "image/tiff",
            ".svg" to "image/svg+xml"
        )
    }
}
