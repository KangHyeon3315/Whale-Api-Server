package com.whale.api.file.application.port.out

import com.whale.api.file.domain.FileResource

interface ReadFileOutput {
    fun readFile(path: String): FileResource

    fun readFileWithRange(
        path: String,
        rangeHeader: String,
    ): FileResource
}
