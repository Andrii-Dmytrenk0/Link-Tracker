package com.linktracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger documentation, available at {@code /swagger-ui.html}.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI linkTrackerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Link Tracker API")
                        .description("REST API for managing influencers and reading click statistics")
                        .version("1.0.0")
                        .contact(new Contact().name("Link Tracker")));
    }
}
