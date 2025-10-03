package com.whale.api.file.application.port.out

interface CreateThumbnailOutput {
    fun createThumbnail(path: String): String
}
