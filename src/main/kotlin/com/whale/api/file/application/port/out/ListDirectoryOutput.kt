package com.whale.api.file.application.port.out

import com.whale.api.file.domain.FileTreeItem

interface ListDirectoryOutput {
    fun listDirectory(directoryPath: String): List<FileTreeItem>
}
