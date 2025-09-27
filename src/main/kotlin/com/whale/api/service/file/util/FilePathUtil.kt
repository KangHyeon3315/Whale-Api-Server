package com.whale.api.service.file.util

import com.whale.api.global.config.MediaFileProperty
import com.whale.api.model.file.exception.FileNotFoundException
import com.whale.api.model.file.exception.InvalidPathException
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class FilePathUtil(
    private val mediaFileProperty: MediaFileProperty
) {
    
    fun validatePath(path: Path) {
        val pathStr = path.toString()
        if (pathStr.contains("..")) {
            throw InvalidPathException("Path contains invalid characters: $pathStr")
        }

        if (!Files.exists(path)) {
            throw InvalidPathException("Path does not exist: $pathStr")
        }
    }
    
    fun resolveBasePath(relativePath: String): Path {
        val basePath = Paths.get(mediaFileProperty.basePath)
        return basePath.resolve(relativePath)
    }
    
    fun validateFileExists(path: Path, errorMessage: String? = null) {
        if (!Files.isRegularFile(path)) {
            throw FileNotFoundException(errorMessage ?: "File not found: $path")
        }
    }
    
    fun validateDirectoryExists(path: Path, errorMessage: String? = null) {
        if (!Files.isDirectory(path)) {
            throw FileNotFoundException(errorMessage ?: "Directory not found: $path")
        }
    }
    
    fun getFileExtension(path: Path): String {
        return path.toString().substringAfterLast('.').lowercase()
    }
    
    fun isImageFile(extension: String): Boolean {
        return extension in MediaFileProperty.IMAGE_EXTENSIONS.map { it.removePrefix(".") }
    }
    
    fun isVideoFile(extension: String): Boolean {
        return extension in MediaFileProperty.VIDEO_EXTENSIONS.map { it.removePrefix(".") }
    }
    
    fun createThumbnailPath(originalPath: String): Path {
        val basePath = Paths.get(mediaFileProperty.basePath)
        val thumbnailDir = basePath.resolve(mediaFileProperty.thumbnailPath)
        Files.createDirectories(thumbnailDir)
        return thumbnailDir.resolve("$originalPath.thumbnail.jpg")
    }
    
    fun createFileGroupPath(identifier: String): Path {
        val basePath = Paths.get(mediaFileProperty.basePath)
        return basePath.resolve(Paths.get(mediaFileProperty.filesPath, "group", identifier))
    }
    
    fun createSingleFilePath(identifier: String, extension: String): Path {
        val basePath = Paths.get(mediaFileProperty.basePath)
        return basePath.resolve(Paths.get(mediaFileProperty.filesPath, "single", "$identifier.$extension"))
    }
}
