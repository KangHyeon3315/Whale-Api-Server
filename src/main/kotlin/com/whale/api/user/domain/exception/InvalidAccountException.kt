package com.whale.api.user.domain.exception

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidAccountException(
    throwable: Throwable? = null,
) : BusinessException(
        HttpStatus.UNAUTHORIZED.value(),
        "Invalid Account",
        throwable,
    )
