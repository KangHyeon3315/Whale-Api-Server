package com.whale.api.file.application.port.out

interface FindFileHashOutput {
    fun existByHash(hash: String): Boolean
}
