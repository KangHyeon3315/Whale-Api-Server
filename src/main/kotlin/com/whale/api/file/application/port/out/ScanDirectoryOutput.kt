package com.whale.api.file.application.port.out

import java.nio.file.Path

data class FileInfo(
    val path: Path,
    val relativePath: String,
    val name: String,
    val extension: String,
)

interface ScanDirectoryOutput {
    fun scanDirectory(directoryPath: String): List<FileInfo>

    fun getVideoEncoding(filePath: String): String?
}
