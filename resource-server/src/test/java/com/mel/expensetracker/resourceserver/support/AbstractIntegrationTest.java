package com.mel.expensetracker.resourceserver.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Same pattern as authorization-server's own {@code AbstractIntegrationTest}:
 * one Testcontainers Postgres for the whole test run, started directly
 * (not via {@code @Container}/{@code @Testcontainers}, whose per-class
 * lifecycle fights Spring's cached {@code ApplicationContext}), and
 * {@code RestTestClient} built by hand against the real port since Boot 4.1
 * doesn't auto-configure it for {@code RANDOM_PORT} tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    protected RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }
}
