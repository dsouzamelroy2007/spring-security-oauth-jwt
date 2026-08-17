package com.mel.expensetracker.authserver.client;

import java.time.Duration;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

/**
 * [FEATURE B9] Seeds the two demo clients on startup, idempotently. Building
 * these with the RegisteredClient builder and saving through the repository
 * (rather than an INSERT in a Flyway migration) lets JdbcRegisteredClientRepository's
 * own mapper serialize client_settings/token_settings, instead of hand-writing
 * framework-internal JSON that's fragile across Spring Security versions.
 */
@Component
public class RegisteredClientSeeder implements ApplicationRunner {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisteredClientSeeder(
            RegisteredClientRepository registeredClientRepository, PasswordEncoder passwordEncoder) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIfAbsent(this::bffClient);
        seedIfAbsent(this::exportWorker);
    }

    private void seedIfAbsent(java.util.function.Supplier<RegisteredClient> factory) {
        RegisteredClient client = factory.get();
        if (registeredClientRepository.findByClientId(client.getClientId()) == null) {
            registeredClientRepository.save(client);
        }
    }

    private RegisteredClient bffClient() {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes("bff-client".getBytes()).toString())
                .clientId("bff-client")
                // Demo secret only, obviously fake -- this is a public portfolio repo.
                .clientSecret(passwordEncoder.encode("bff-client-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/bff-client")
                // [FEATURE B4] RP-initiated logout: Spring Authorization Server only
                // redirects back to a post-logout URI that's registered here -- an
                // unregistered one is silently ignored, not an error.
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("expenses.read")
                .scope("expenses.write")
                .clientSettings(ClientSettings.builder()
                        // Confidential client, but PKCE is still required: defense in
                        // depth against authorization-code interception even if the
                        // client secret itself leaks in a lower environment.
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofHours(8))
                        .reuseRefreshTokens(false)
                        .build())
                .build();
    }

    private RegisteredClient exportWorker() {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes("export-worker".getBytes()).toString())
                .clientId("export-worker")
                .clientSecret(passwordEncoder.encode("export-worker-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("expenses.export")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(false)
                        // No end user in this grant -- consent has no meaning here.
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();
    }
}
