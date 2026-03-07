package com.agentic.subscription.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for OCI and Spring beans
 * Initializes OpenAPI/Swagger documentation and database configurations
 */
@Configuration
public class OciConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(OciConfiguration.class);
    
    /**
     * Configure OpenAPI documentation for Swagger UI
     * Provides API metadata and information
     * 
     * @return OpenAPI configuration bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        logger.info("Initializing OpenAPI/Swagger documentation");
        
        return new OpenAPI()
            .info(new Info()
                .title("User Subscription API")
                .version("1.0.0")
                .description("REST API for managing user subscriptions with Autonomous AI Database integration")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@agentic.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

