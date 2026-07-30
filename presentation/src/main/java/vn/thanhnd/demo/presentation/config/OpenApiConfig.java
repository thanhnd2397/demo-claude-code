package vn.thanhnd.demo.presentation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata and JWT bearer security scheme, matching the {@code Authorization: Bearer
 * <token>} header read by {@link vn.thanhnd.demo.presentation.filter.JwtAuthenticationFilter}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    /**
     * Defines the OpenAPI document metadata and the bearer-token security scheme used by every
     * protected endpoint.
     *
     * @return the {@link OpenAPI} bean consumed by springdoc to generate {@code /v3/api-docs}
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("thanhnd-demo-claude API")
                        .description("REST API cho hệ thống quản trị (Administrator)")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
    }
}
