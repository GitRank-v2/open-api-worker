package com.dragonguard.open_api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executors

@Configuration
class AsyncConfig {
    @Bean(name = ["virtualAsyncTaskExecutor"])
    fun virtualAsyncTaskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.setTaskDecorator { runnable: Runnable ->
            Runnable {
                Executors.newVirtualThreadPerTaskExecutor().use { scope ->
                    scope.submit(runnable)
                }
            }
        }
        executor.initialize()
        return executor
    }
}
