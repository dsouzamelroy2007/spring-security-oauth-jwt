package com.mel.expensetracker.authserver.config.jwk;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * [FEATURE B5] A separate bean from {@link JwkSourceConfig} on purpose:
 * constructor-injecting RotatingRsaKeyManager into the same @Configuration
 * class that also declares its @Bean factory method is a self-referential
 * cycle Spring refuses to start ("jwkSourceConfig defined in ... forms a
 * cycle"). @Scheduled also requires a no-arg method, which rules out passing
 * the manager as a method parameter instead.
 */
@Component
@EnableScheduling
public class RsaKeyRotationScheduler {

    private final RotatingRsaKeyManager keyManager;

    public RsaKeyRotationScheduler(RotatingRsaKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    // initialDelay = the same interval: without it, @Scheduled's default
    // initial delay of zero would rotate immediately on every startup,
    // leaving two keys present the instant the app boots instead of only
    // after a real interval has passed.
    @Scheduled(
            initialDelayString = "${app.security.jwk-rotation-interval}",
            fixedDelayString = "${app.security.jwk-rotation-interval}")
    public void rotate() {
        keyManager.rotate();
    }
}
