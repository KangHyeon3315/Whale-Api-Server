package com.whale.api.file.application.port.`in`

import com.whale.api.file.application.port.`in`.command.SaveFileCommand

interface SaveFileUseCase {
    fun requestSave(command: SaveFileCommand)

    fun save()
}
