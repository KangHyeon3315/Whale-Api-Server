package com.whale.api.file.application.port.out

import com.whale.api.file.domain.File

interface SaveFileOutput {
    fun save(file: File): File
}
