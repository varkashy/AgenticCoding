package com.agentic.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot Application for UserSubscription service
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.agentic.subscription")
public class UserSubscriptionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserSubscriptionApplication.class, args);
    }
}
