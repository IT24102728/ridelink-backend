package com.ridelink.ride_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI rideOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink — Ride Management Service")
                        .version("1.0.0")
                        .description("Ride lifecycle APIs with interservice calls to Driver & Fare services"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}