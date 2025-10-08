package com.whale.api.archive.application.port.out

interface ReadArchiveItemContentOutput {
    fun readTextContent(storedPath: String): String

    fun readTextContentPreview(
        storedPath: String,
        maxLength: Int,
    ): String

    fun fileExists(storedPath: String): Boolean
}
