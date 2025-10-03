package com.whale.api.file.application.port.out

import com.whale.api.file.domain.Tag

interface FindTagOutput {
    fun findAllByNameInAndTypeIn(
        names: List<String>,
        types: List<String>,
    ): List<Tag>
}
