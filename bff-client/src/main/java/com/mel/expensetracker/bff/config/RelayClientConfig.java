package com.mel.expensetracker.bff.config;

import com.mel.expensetracker.bff.relay.ResourceServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * [FEATURE B1] [FEATURE B3] Wires the {@link ResourceServerClient} proxy on
 * top of a {@link RestClient} carrying {@link OAuth2ClientHttpRequestInterceptor}.
 * That interceptor resolves the current request's {@code Authentication},
 * asks Boot's auto-configured {@link OAuth2AuthorizedClientManager} for its
 * authorized client, attaches {@code Authorization: Bearer <token>}, and --
 * because the manager's default provider chain includes a refresh-token
 * provider -- transparently exchanges an expired access token for a new
 * access+refresh pair (persisted back into the Redis-backed session) before
 * the request goes out. None of that is bespoke code; it's what Boot already
 * wires once {@code spring-boot-starter-security-oauth2-client} is present.
 */
@Configuration
public class RelayClientConfig {

    @Bean
    ResourceServerClient resourceServerClient(
            RestClient.Builder restClientBuilder,
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${app.resource-server.base-url}") String resourceServerBaseUrl) {
        RestClient restClient = restClientBuilder
                .baseUrl(resourceServerBaseUrl)
                .requestInterceptor(new OAuth2ClientHttpRequestInterceptor(authorizedClientManager))
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(ResourceServerClient.class);
    }
}
