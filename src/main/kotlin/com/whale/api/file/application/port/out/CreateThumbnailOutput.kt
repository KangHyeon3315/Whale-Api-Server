package com.whale.api.file.application.port.out

import java.nio.file.Path

interface CreateThumbnailOutput {

    fun createThumbnail(path: String): String

}
