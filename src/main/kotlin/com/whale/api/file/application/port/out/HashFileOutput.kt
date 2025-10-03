package com.whale.api.file.application.port.out


interface HashFileOutput {

    fun calculateFileHash(path: String): String

}
