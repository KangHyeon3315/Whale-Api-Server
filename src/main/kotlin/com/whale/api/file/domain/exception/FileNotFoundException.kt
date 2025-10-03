package com.whale.api.file.domain.exception

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class FileNotFoundException(
    message: String = "File not found",
    throwable: Throwable? = null,
) : BusinessException(
    HttpStatus.NOT_FOUND.value(),
    message,
    throwable,
)
