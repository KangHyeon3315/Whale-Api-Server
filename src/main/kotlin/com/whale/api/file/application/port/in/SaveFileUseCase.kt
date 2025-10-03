package com.whale.api.file.application.port.`in`

import com.whale.api.file.application.port.`in`.command.SaveFileCommand
import com.whale.api.file.domain.File

interface SaveFileUseCase {

    fun requestSave(command: SaveFileCommand)

    fun save()
}
