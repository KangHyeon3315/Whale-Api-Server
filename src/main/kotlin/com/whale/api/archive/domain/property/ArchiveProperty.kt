package com.whale.api.archive.domain.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ArchiveProperty(
    @Value("\${archive.base-path:/archives}")
    val basePath: String,
    // 1000MB
    @Value("\${archive.max-file-size:1048576000}")
    val maxFileSize: Long,
    @Value(
        "\${archive.allowed-extensions:" +
            ".jpg,.jpeg,.png,.gif,.bmp,.webp,.tiff,.heic,.heif,.mp4,.mov,.m4v," +
            ".txt,.md,.json,.xml,.csv,.log,.rtf,.doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx}",
    )
    val allowedExtensions: String,
) {
    val archivePath: String = "archives"
    val metadataPath: String = "metadata"
    val thumbnailPath: String = "thumbnails"
    val livePhotoPath: String = "live-photos"
    val documentsPath: String = "documents"
    val textFilesPath: String = "text-files"

    val imageExtensions = setOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".heic", ".heif")
    val videoExtensions = setOf(".mp4", ".mov", ".m4v")
    val livePhotoExtensions = setOf(".heic", ".heif") // Live Photos are typically HEIC/HEIF
    val textExtensions = setOf(".txt", ".md", ".json", ".xml", ".csv", ".log", ".rtf")
    val documentExtensions = setOf(".doc", ".docx", ".pdf", ".xls", ".xlsx", ".ppt", ".pptx")

    val mimeTypeMapping =
        mapOf(
            // Images
            ".jpg" to "image/jpeg",
            ".jpeg" to "image/jpeg",
            ".png" to "image/png",
            ".gif" to "image/gif",
            ".bmp" to "image/bmp",
            ".webp" to "image/webp",
            ".tiff" to "image/tiff",
            ".heic" to "image/heic",
            ".heif" to "image/heif",
            // Videos
            ".mp4" to "video/mp4",
            ".mov" to "video/quicktime",
            ".m4v" to "video/x-m4v",
            // Text files
            ".txt" to "text/plain",
            ".md" to "text/markdown",
            ".json" to "application/json",
            ".xml" to "application/xml",
            ".csv" to "text/csv",
            ".log" to "text/plain",
            ".rtf" to "application/rtf",
            // Documents
            ".doc" to "application/msword",
            ".docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            ".pdf" to "application/pdf",
            ".xls" to "application/vnd.ms-excel",
            ".xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ".ppt" to "application/vnd.ms-powerpoint",
            ".pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        )

    fun getAllowedExtensionsList(): List<String> {
        return allowedExtensions.split(",").map { it.trim() }
    }

    fun isAllowedExtension(extension: String): Boolean {
        return getAllowedExtensionsList().contains(extension.lowercase())
    }

    fun getMimeType(extension: String): String {
        return mimeTypeMapping[extension.lowercase()] ?: "application/octet-stream"
    }

    fun isImageFile(extension: String): Boolean {
        return imageExtensions.contains(extension.lowercase())
    }

    fun isVideoFile(extension: String): Boolean {
        return videoExtensions.contains(extension.lowercase())
    }

    fun isLivePhotoFile(extension: String): Boolean {
        return livePhotoExtensions.contains(extension.lowercase())
    }

    fun isTextFile(extension: String): Boolean {
        return textExtensions.contains(extension.lowercase())
    }

    fun isDocumentFile(extension: String): Boolean {
        return documentExtensions.contains(extension.lowercase())
    }

    fun getFileCategory(extension: String): String {
        return when {
            isImageFile(extension) -> "image"
            isVideoFile(extension) -> "video"
            isTextFile(extension) -> "text"
            isDocumentFile(extension) -> "document"
            isLivePhotoFile(extension) -> "live_photo"
            else -> "other"
        }
    }
}
