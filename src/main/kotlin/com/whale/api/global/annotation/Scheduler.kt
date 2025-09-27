package com.whale.api.global.annotation

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Profile("schedule")
annotation class Scheduler
