package com.whale.api.service.file.util

import com.whale.api.controller.file.response.FileTreeItem
import com.whale.api.global.config.MediaFileProperty
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class FileTreeBuilder(
    private val mediaFileProperty: MediaFileProperty
) {
    
    fun buildFileTree(targetPath: Path): List<FileTreeItem> {
        return if (!Files.exists(targetPath) || !Files.isDirectory(targetPath)) {
            emptyList()
        } else {
            Files.list(targetPath).use { stream ->
                stream.map { path ->
                    createFileTreeItem(path)
                }.toList()
            }
        }
    }
    
    private fun createFileTreeItem(path: Path): FileTreeItem {
        val fileName = path.fileName.toString()
        val isDirectory = Files.isDirectory(path)
        val extension = if (!isDirectory) {
            val dotIndex = fileName.lastIndexOf('.')
            if (dotIndex > 0) fileName.substring(dotIndex + 1).lowercase() else ""
        } else ""

        return FileTreeItem(
            name = fileName,
            isDir = isDirectory,
            extension = extension
        )
    }
    
    private fun determineMediaType(fileName: String): String? {
        val extension = fileName.substringAfterLast('.').lowercase()
        
        return when {
            extension in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") } -> "image"
            extension in MediaFileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") } -> "video"
            else -> null
        }
    }
}
