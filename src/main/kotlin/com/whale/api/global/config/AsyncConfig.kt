package com.whale.api.global.config

import com.whale.api.global.exception.AsyncExceptionHandler
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

private const val CORE_POOL_SIZE = 100
private const val MAX_POOL_SIZE = 300
private const val QUEUE_CAPACITY = 500

@Configuration
class AsyncConfig : AsyncConfigurer {
    override fun getAsyncExecutor(): Executor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = CORE_POOL_SIZE
        taskExecutor.maxPoolSize = MAX_POOL_SIZE
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY)
        taskExecutor.setThreadNamePrefix("async-exec-")
        taskExecutor.setRejectedExecutionHandler(CallerRunsPolicyWithAlertLogging())
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        taskExecutor.initialize()
        return taskExecutor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncExceptionHandler()
    }
}
