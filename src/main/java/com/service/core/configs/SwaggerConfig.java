package com.service.core.configs;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("OAuth2 авторизация через Yandex")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl("https://oauth.yandex.com/authorize")
                                                .tokenUrl("https://oauth.yandex.com/token")
                                                .scopes(new Scopes()
                                                        .addString("login:info", "Доступ к информации о пользователе")
                                                        .addString("login:email", "Доступ к email")
                                                )
                                        )
                                )
                        )
                )
                .path("/oauth2/authorization/yandex", new PathItem()
                        .get(new Operation()
                                .summary("Начать OAuth 2.0 авторизацию через Yandex")
                                .description("Перенаправляет на страницу авторизации Yandex")
                                .addTagsItem("Authentication")
                                .responses(new io.swagger.v3.oas.models.responses.ApiResponses()
                                        .addApiResponse("302", new io.swagger.v3.oas.models.responses.ApiResponse()
                                                .description("Перенаправление на Yandex OAuth"))
                                )
                        )
                );
    }
}
