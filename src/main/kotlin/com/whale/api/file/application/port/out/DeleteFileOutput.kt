package com.whale.api.file.application.port.out

interface DeleteFileOutput {
    fun deleteFile(path: String)
}
