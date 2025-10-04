package com.whale.api.file.application.port.`in`

import com.whale.api.file.domain.FileTreeItem

enum class SortType {
    NAME,
    NUMBER,
}

interface GetUnsortedTreeUseCase {
    fun getUnsortedTree(
        path: String,
        cursor: String?,
        limit: Int,
        sort: SortType,
    ): List<FileTreeItem>
}
