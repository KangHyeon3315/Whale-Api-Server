package com.whale.api.file.application.port.out

import com.whale.api.file.domain.File
import com.whale.api.file.domain.FileTag
import com.whale.api.file.domain.Tag

interface SaveFileTagOutput {

    fun saveAll(tags: List<FileTag>): List<FileTag>
}
