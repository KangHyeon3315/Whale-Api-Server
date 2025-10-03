package com.whale.api.file.application.port.out

import com.whale.api.file.domain.SaveTask

interface SaveSaveTaskOutput {

    fun save(task: SaveTask): SaveTask
}
