package com.insurance.sample.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI insuranceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Insurance Sample API")
                        .description("APAC Commercial Insurance Platform — Policy BFF Service")
                        .version("1.0.0")
                        .contact(new Contact().name("Insurance Platform Team")));
    }
}
