package com.whale.api.file.domain.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class FileProperty(

    @Value("\${file.path}")
    private val path: String
) {

}
