package com.whale.api.controller

import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    private val logger = KotlinLogging.logger { }

    @GetMapping("/health_check")
    fun health(): String {
        logger.trace { "Health check" }
        return "OK"
    }
}
