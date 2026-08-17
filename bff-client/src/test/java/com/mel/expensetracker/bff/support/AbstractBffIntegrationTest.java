package com.mel.expensetracker.bff.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Same pattern as authorization-server's and resource-server's own
 * {@code AbstractIntegrationTest}: real Postgres (for the audit writer) and
 * real Redis (for the session repository), started once and never stopped
 * explicitly (Ryuk reaps both on JVM exit), never {@code @Container}/
 * {@code @Testcontainers} -- see those classes' comments for why that JUnit
 * extension's per-class teardown fights Spring's cached
 * {@code ApplicationContext}. {@code RestTestClient} built by hand against
 * the real port, since Boot 4.1 doesn't auto-configure it for
 * {@code RANDOM_PORT} tests.
 *
 * <p>[FEATURE D8] bff-client owns no Flyway migrations -- {@code audit_log}
 * is authorization-server's table. This test-only Postgres never runs
 * authorization-server's migrations, so the table is created by hand here,
 * matching {@code V6__create_audit_log.sql}'s shape minus the {@code org_id}
 * foreign key (no bff-client-triggered event ever populates {@code org_id},
 * so there's no {@code organisations} table to reference). A real,
 * migration-created copy of this same table is exercised by
 * authorization-server's own tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractBffIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
        createAuditLogTable();
    }

    private static void createAuditLogTable() {
        try (Connection connection =
                        DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS audit_log (
                        id           bigserial PRIMARY KEY,
                        occurred_at  timestamptz NOT NULL DEFAULT now(),
                        event_type   varchar(100) NOT NULL,
                        principal    varchar(200),
                        org_id       uuid,
                        ip_address   varchar(45),
                        detail       jsonb
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create test audit_log table", e);
        }
    }

    @LocalServerPort
    protected int port;

    protected RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }
}
