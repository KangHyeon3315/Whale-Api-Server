package com.whale.api.file.application.port.`in`

import com.whale.api.file.domain.FileResource

interface GetFileUseCase {
    fun getUnsortedImage(path: String): FileResource

    fun getVideo(
        path: String,
        rangeHeader: String? = null,
    ): FileResource
}
