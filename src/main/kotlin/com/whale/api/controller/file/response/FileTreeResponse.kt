package com.whale.api.controller.file.response

data class FileTreeResponse(
    val files: List<FileTreeItem>
)

data class FileTreeItem(
    val name: String,
    val isDir: Boolean,
    val extension: String
)
