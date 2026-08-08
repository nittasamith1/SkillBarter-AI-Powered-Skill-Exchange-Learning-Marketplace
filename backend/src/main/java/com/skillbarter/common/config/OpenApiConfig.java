package com.skillbarter.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI configuration.
 *
 * <p>Accessible at /swagger-ui.html in all environments.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SkillBarter AI API")
                        .version("1.0.0 — Phase 1")
                        .description("""
                                SkillBarter AI — AI-Powered Skill Exchange & Learning Marketplace.

                                **Phase 1**: Foundation + Identity + Multi-Tenancy.

                                This API provides authentication, user management, and tenant operations.
                                Future phases will add skill matching, AI recommendations, and learning sessions.
                                """)
                        .contact(new Contact()
                                .name("SkillBarter Team")
                                .email("dev@skillbarter.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token obtained from POST /api/v1/auth/login")));
    }
}
