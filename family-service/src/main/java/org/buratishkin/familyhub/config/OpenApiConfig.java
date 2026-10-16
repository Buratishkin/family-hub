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
    private static final String INTERNAL_SERVICE_AUTH = "internalServiceAuth";

    @Bean
    public OpenAPI familyServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FamilyHub Family Service API")
                        .version("0.1")
                        .description("Families, members, invites, aliases, family plans and polls."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerAuthScheme())
                        .addSecuritySchemes(INTERNAL_SERVICE_AUTH, internalServiceAuthScheme()));
    }

    @Bean
    public OpenApiCustomizer familyServiceSecurityCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperations().forEach(operation -> applySecurity(path, operation)));
    }

    private void applySecurity(String path, Operation operation) {
        if (path.equals("/health")) {
            operation.setSecurity(List.of());
            return;
        }
        if (path.startsWith("/internal/")) {
            operation.setSecurity(List.of(new SecurityRequirement().addList(INTERNAL_SERVICE_AUTH)));
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

    private SecurityScheme internalServiceAuthScheme() {
        return new SecurityScheme()
                .name("X-FamilyHub-Internal-Token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER);
    }
}
