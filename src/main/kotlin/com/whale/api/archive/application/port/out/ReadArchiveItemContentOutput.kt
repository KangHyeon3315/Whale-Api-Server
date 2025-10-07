package com.whale.api.archive.application.port.out

import java.util.UUID

interface ReadArchiveItemContentOutput {
    fun readTextContent(storedPath: String): String
    fun readTextContentPreview(storedPath: String, maxLength: Int): String
    fun fileExists(storedPath: String): Boolean
}
