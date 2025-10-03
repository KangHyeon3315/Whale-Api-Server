package com.whale.api.file.application.port.out

interface MoveFileOutput {
    fun moveFile(
        sourcePath: String,
        destinationPath: String,
    )
}
