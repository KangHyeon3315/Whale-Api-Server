package com.whale.api.file.application.port.`in`

import com.whale.api.file.domain.Tag

interface GetAllTagsUseCase {
    fun getAllTags(): List<Tag>
}
