package com.whale.api.global.jwt.exceptions

import com.whale.api.global.exception.BusinessException
import org.springframework.http.HttpStatus

class TokenExpiredException : BusinessException(
    errorCode = HttpStatus.UNAUTHORIZED.value(),
    message = "Token has expired",
)
