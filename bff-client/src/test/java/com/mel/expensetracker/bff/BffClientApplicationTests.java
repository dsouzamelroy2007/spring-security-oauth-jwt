package com.mel.expensetracker.bff;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Since M4, this module needs a real Postgres (audit writer) and Redis
 * (session repository) to boot at all -- no longer the dependency-free M1
 * skeleton -- so this smoke test now shares {@link AbstractBffIntegrationTest}'s
 * Testcontainers-backed infrastructure like every other test in this module.
 */
class BffClientApplicationTests extends AbstractBffIntegrationTest {

    @Test
    void contextLoads() {
    }
}
