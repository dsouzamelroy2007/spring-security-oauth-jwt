package com.mel.expensetracker.authserver.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    // Matches docker-compose.yml's dev Postgres image. One container for the
    // whole test run -- never touches the docker-compose service on localhost:5433.
    //
    // Deliberately NOT @Container/@Testcontainers: that JUnit extension stops
    // the container after each test class's afterAll, but Spring's
    // ApplicationContext (and its already-built HikariCP pool) is cached and
    // reused across test classes independently of that lifecycle. The result
    // was a cached DataSource left pointing at a container port that had
    // already been torn down. Starting it once, here, and never stopping it
    // (Ryuk reaps it on JVM exit) keeps both caches consistent.
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    // Not @Autowired: RestTestClient isn't Boot-auto-configured for a random-port
    // SpringBootTest as of Boot 4.1, so it's built directly against the actual port.
    protected RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }
}
