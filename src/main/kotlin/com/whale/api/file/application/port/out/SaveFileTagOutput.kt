package com.whale.api.file.application.port.out

import com.whale.api.file.domain.FileTag

interface SaveFileTagOutput {
    fun saveAll(tags: List<FileTag>): List<FileTag>
}
