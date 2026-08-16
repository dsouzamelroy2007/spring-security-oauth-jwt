package com.mel.expensetracker.authserver.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * [FEATURE A4] A hand-built {@link DelegatingPasswordEncoder}, not
 * {@link org.springframework.security.crypto.factory.PasswordEncoderFactories#createDelegatingPasswordEncoder()}.
 *
 * <p>That factory's current (encode-with) id is "bcrypt", so an already-bcrypt
 * hash would never be flagged for upgrade — {@code PasswordEncoder.upgradeEncoding()}
 * only returns true when the stored id differs from the current one. Pinning
 * "argon2" as the current id here is what makes bcrypt -> argon2 upgrade-on-login
 * (the M2 acceptance test) actually happen.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = Map.of(
                "bcrypt", new BCryptPasswordEncoder(),
                "argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return new DelegatingPasswordEncoder("argon2", encoders);
    }
}
