package com.whale.api.file.application.port.out

import com.whale.api.file.domain.Tag

interface FindAllTagsOutput {
    fun findAllTags(): List<Tag>
}
