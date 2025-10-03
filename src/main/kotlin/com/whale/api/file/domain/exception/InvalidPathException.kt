package com.whale.api.file.domain.exception

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidPathException(
    message: String = "Invalid file path",
    throwable: Throwable? = null,
) : BusinessException(
    HttpStatus.BAD_REQUEST.value(),
    message,
    throwable,
)
