package com.whale.api.file.domain

data class FileTreeItem(
    val name: String,
    val isDir: Boolean,
    val extension: String,
)
