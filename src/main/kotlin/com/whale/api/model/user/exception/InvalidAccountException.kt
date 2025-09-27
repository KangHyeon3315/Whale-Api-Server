package com.whale.api.model.user.exception

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidAccountException(
    throwable: Throwable? = null,
) : BusinessException(
        HttpStatus.UNAUTHORIZED.value(),
        "Invalid Account",
        throwable,
    )
