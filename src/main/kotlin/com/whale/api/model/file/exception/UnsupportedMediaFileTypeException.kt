package com.whale.api.model.file.exception

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class UnsupportedMediaFileTypeException(
    message: String = "Unsupported media file type",
    throwable: Throwable? = null,
) : BusinessException(
    HttpStatus.BAD_REQUEST.value(),
    message,
    throwable,
)
