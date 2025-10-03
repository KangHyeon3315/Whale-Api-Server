package com.whale.api.file.domain.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class FileProperty(
    @Value("\${file.base-path}")
    val basePath: String,
) {
    val thumbnailPath: String = "thumbnail"
    val unsortedPath: String = "unsorted"
    val filesPath: String = "files"

    val videoExtensions = setOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v")
    val imageExtensions = setOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".svg")

    val mimeTypeMapping =
        mapOf(
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
            ".svg" to "image/svg+xml",
        )
}
