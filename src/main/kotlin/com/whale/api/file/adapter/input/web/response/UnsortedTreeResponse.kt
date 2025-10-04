package com.whale.api.file.adapter.input.web.response

data class FileTreeItemDto(
    val name: String,
    val isDir: Boolean,
    val extension: String,
)

data class UnsortedTreeResponse(
    val files: List<FileTreeItemDto>,
)
