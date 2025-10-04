package com.whale.api.file.application.port.out

interface ValidateFilePathOutput {
    fun validatePath(path: String)

    fun isImageFile(path: String): Boolean

    fun isVideoFile(path: String): Boolean
}
