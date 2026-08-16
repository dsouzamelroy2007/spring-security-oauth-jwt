package com.mel.expensetracker.authserver.config.jwk;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.RSAKey;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RotatingRsaKeyManagerTest {

    @Test
    void startsWithExactlyOneSigningKey() {
        RotatingRsaKeyManager manager = new RotatingRsaKeyManager();

        assertThat(manager.currentJwkSet().getKeys()).hasSize(1);
    }

    @Test
    void rotateKeepsThePreviousKeyForVerification() {
        RotatingRsaKeyManager manager = new RotatingRsaKeyManager();
        String originalKid = firstKey(manager).getKeyID();

        manager.rotate();

        List<RSAKey> keys = rsaKeys(manager);
        assertThat(keys).hasSize(2);
        assertThat(keys.get(0).getKeyID()).isNotEqualTo(originalKid);
        assertThat(keys.get(1).getKeyID()).isEqualTo(originalKid);
    }

    @Test
    void rotationRetentionIsBoundedToTwoGenerations() {
        RotatingRsaKeyManager manager = new RotatingRsaKeyManager();
        String originalKid = firstKey(manager).getKeyID();

        manager.rotate();
        manager.rotate();

        List<RSAKey> keys = rsaKeys(manager);
        assertThat(keys).hasSize(2);
        assertThat(keys).extracting(RSAKey::getKeyID).doesNotContain(originalKid);
    }

    private static RSAKey firstKey(RotatingRsaKeyManager manager) {
        return rsaKeys(manager).get(0);
    }

    private static List<RSAKey> rsaKeys(RotatingRsaKeyManager manager) {
        return manager.currentJwkSet().getKeys().stream()
                .map(RSAKey.class::cast)
                .collect(Collectors.toList());
    }
}
