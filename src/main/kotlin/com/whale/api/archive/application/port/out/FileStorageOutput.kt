package com.whale.api.archive.application.port.out

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface FileStorageOutput {
    fun storeFile(file: MultipartFile, relativePath: String): String
    fun storeFile(inputStream: InputStream, fileName: String, relativePath: String): String
    fun deleteFile(filePath: String): Boolean
    fun fileExists(filePath: String): Boolean
    fun getFileSize(filePath: String): Long
    fun calculateChecksum(file: MultipartFile): String
    fun calculateChecksum(inputStream: InputStream): String
}
