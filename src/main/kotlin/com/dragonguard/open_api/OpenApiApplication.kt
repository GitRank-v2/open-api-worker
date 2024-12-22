package com.dragonguard.open_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableRetry
@SpringBootApplication
class OpenApiApplication

fun main(args: Array<String>) {
    runApplication<OpenApiApplication>(*args)
}
