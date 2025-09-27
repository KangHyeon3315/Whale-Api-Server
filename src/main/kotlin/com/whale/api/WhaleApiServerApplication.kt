package com.whale.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WhaleApiServerApplication

fun main(args: Array<String>) {
    runApplication<WhaleApiServerApplication>(*args)
}
