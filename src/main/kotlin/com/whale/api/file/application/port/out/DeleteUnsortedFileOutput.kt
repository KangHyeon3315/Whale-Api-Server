package com.whale.api.file.application.port.out

interface DeleteUnsortedFileOutput {
    fun deleteByPath(path: String)
}
