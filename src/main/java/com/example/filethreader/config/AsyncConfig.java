package com.example.filethreader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Enables Spring's asynchronous method execution capability
public class AsyncConfig {

    /**
     * Configures a ThreadPoolTaskExecutor for managing asynchronous tasks.
     *
     * @return an Executor instance configured with specific parameters
     */
    @Bean(name = "taskExecutor")
    public Executor TaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Set the core number of threads
        executor.setCorePoolSize(10); // The minimum number of threads to keep in the pool

        // Set the maximum number of threads
        executor.setMaxPoolSize(10); // The maximum number of threads to allow in the pool

        // Set the capacity of the queue
        executor.setQueueCapacity(100); // The capacity for the thread pool queue

        // Set the prefix for thread names
        executor.setThreadNamePrefix("userThread-"); // Prefix for naming threads for easier debugging

        executor.initialize(); // Initialize the executor

        return executor; // Return the configured executor
    }
}
