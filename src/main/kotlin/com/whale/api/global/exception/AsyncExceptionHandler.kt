package com.whale.api.global.exception

import mu.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    private val logger = KotlinLogging.logger {}

    override fun handleUncaughtException(
        e: Throwable,
        method: Method,
        vararg params: Any,
    ) {
        logger.error(e.message, e)
    }
}
