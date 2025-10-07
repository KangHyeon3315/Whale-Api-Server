package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveFileResource
import java.util.UUID

interface ReadArchiveFileOutput {
    fun readArchiveFile(storedPath: String, fileName: String, mimeType: String): ArchiveFileResource
    fun readArchiveFileWithRange(storedPath: String, fileName: String, mimeType: String, rangeHeader: String): ArchiveFileResource
    fun createThumbnail(storedPath: String, mimeType: String): String
    fun readThumbnail(thumbnailPath: String, fileName: String): ArchiveFileResource
    fun validateFilePath(storedPath: String): Boolean
    fun isImageFile(mimeType: String): Boolean
    fun isVideoFile(mimeType: String): Boolean
}
