package com.whale.api.global.config

import mu.KotlinLogging
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

class CallerRunsPolicyWithAlertLogging : RejectedExecutionHandler {
    private val logger = KotlinLogging.logger {}

    override fun rejectedExecution(
        r: Runnable,
        executor: ThreadPoolExecutor,
    ) {
        logger.warn("[ALERT] Async task rejected. Please check async thread pool size and queue capacity")
        if (!executor.isShutdown) {
            r.run()
        }
    }
}
