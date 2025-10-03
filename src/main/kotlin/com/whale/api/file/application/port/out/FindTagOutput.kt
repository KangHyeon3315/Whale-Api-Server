package com.whale.api.file.application.port.out

import com.whale.api.file.domain.SaveTask
import com.whale.api.file.domain.Tag
import com.whale.api.file.domain.enums.SaveTaskStatus
import java.util.UUID

interface FindTagOutput {

    fun findAllByNameInAndTypeIn(names: List<String>, types: List<String>): List<Tag>

}
