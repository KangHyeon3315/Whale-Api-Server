package com.whale.api.file.application.port.`in`

import com.whale.api.file.domain.FileResource

interface GetThumbnailUseCase {
    fun getThumbnail(path: String): FileResource
}
