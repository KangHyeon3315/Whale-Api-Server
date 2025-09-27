package com.whale.api.global.exception

object ErrorCodeFactory {
    fun createErrorCode(ex: Throwable): Int {
        return when (ex) {
            is IllegalArgumentException -> 400
            is BusinessException -> ex.errorCode
            else -> 500
        }
    }
}
