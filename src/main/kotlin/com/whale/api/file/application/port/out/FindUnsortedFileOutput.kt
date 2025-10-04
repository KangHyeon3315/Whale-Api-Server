package com.whale.api.file.application.port.out

import com.whale.api.file.domain.UnsortedFile

interface FindUnsortedFileOutput {
    fun findByPath(path: String): UnsortedFile?
}
