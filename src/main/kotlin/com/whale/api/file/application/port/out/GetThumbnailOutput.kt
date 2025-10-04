package com.whale.api.file.application.port.out

import com.whale.api.file.domain.FileResource

interface GetThumbnailOutput {
    fun getThumbnail(path: String): FileResource
}
