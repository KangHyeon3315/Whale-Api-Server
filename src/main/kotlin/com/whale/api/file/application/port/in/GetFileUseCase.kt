package com.whale.api.file.application.port.`in`

import com.whale.api.file.domain.FileResource

interface GetFileUseCase {
    fun getImage(path: String): FileResource

    fun getVideo(
        path: String,
        rangeHeader: String? = null,
    ): FileResource
}
