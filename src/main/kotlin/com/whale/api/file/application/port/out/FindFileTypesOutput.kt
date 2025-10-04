package com.whale.api.file.application.port.out

interface FindFileTypesOutput {
    fun findAllDistinctTypes(): List<String>
}
