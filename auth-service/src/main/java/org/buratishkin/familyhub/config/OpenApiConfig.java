package org.buratishkin.familyhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FamilyHub Auth Service API")
                        .version("0.1")
                        .description("Registration, login, refresh token and logout endpoints."))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerAuthScheme()));
    }

    @Bean
    public OpenApiCustomizer authServiceSecurityCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperations().forEach(operation -> applySecurity(path, operation)));
    }

    private void applySecurity(String path, Operation operation) {
        if (path.equals("/health") || path.startsWith("/auth/")) {
            operation.setSecurity(List.of());
            return;
        }
        operation.setSecurity(List.of(new SecurityRequirement().addList(BEARER_AUTH)));
    }

    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
