package com.whale.api.file.application.port.out

import com.whale.api.file.domain.Tag

interface SaveTagOutput {
    fun saveAll(tags: List<Tag>): List<Tag>
}
