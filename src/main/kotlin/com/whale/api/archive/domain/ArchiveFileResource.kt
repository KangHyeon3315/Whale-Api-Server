package com.whale.api.archive.domain

import java.io.InputStream

data class ArchiveFileResource(
    val itemIdentifier: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val inputStream: InputStream,
    val isRangeRequest: Boolean = false,
    val rangeStart: Long = 0,
    val rangeEnd: Long = 0,
) {
    val contentLength: Long
        get() = if (isRangeRequest) rangeEnd - rangeStart + 1 else size

    fun isPartialContent(): Boolean = isRangeRequest
}
