package com.whale.api.global.exception

open class BusinessException(
    open val errorCode: Int,
    override val message: String,
    throwable: Throwable? = null
) : RuntimeException(message, throwable) {
}
