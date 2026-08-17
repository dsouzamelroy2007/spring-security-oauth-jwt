package com.mel.expensetracker.integrationtests.support;

import com.mel.expensetracker.authserver.AuthorizationServerApplication;
import com.mel.expensetracker.resourceserver.ResourceServerApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Boots authorization-server and resource-server as two independent Spring
 * contexts in one JVM, sharing one Testcontainers Postgres -- reuses
 * authorization-server's own "start once, never stop" container pattern (see
 * its {@code AbstractIntegrationTest}), since {@code @Container}/
 * {@code @Testcontainers}' per-class lifecycle fights Spring's cached
 * {@code ApplicationContext}.
 *
 * <p>Both servers run on their real, fixed dev ports (9000, 8082) rather than
 * random ports: authorization-server's issuer is baked into every claim it
 * mints, and resource-server's {@code JwtDecoderConfig} is configured against
 * {@code http://localhost:9000} in its own {@code application.yml} -- keeping
 * the real ports means neither app needs a test-only property override for
 * the other's address. Tradeoff: a port collision with an already-running dev
 * instance on this machine is possible but unlikely, and a non-issue in CI.
 *
 * <p>Both modules' {@code application.yml} live at the identical classpath
 * resource path ({@code application.yml}, root of each jar). With both jars
 * on this module's combined test classpath, Boot's default config-file
 * resolution does not hand each context "its own" file the way it would
 * across two separate JVMs/classloaders -- empirically, <em>both</em>
 * contexts end up loading authorization-server's file (first on the
 * classpath, by dependency declaration order), and resource-server's own
 * {@code application.yml} is never seen by its own context at all. Every
 * property resource-server actually needs (Flyway location/schema, the
 * issuer-uri its {@code JwtDecoderConfig} requires, the expected audience,
 * {@code open-in-view: false}) is therefore passed explicitly below, as
 * command-line-style {@code --key=value} arguments to {@code run(...)} --
 * not {@code SpringApplicationBuilder.properties(...)}, which calls
 * {@code SpringApplication.setDefaultProperties(...)} and is Boot's
 * *lowest*-precedence property source, so it would still lose to whichever
 * {@code application.yml} wins the classpath race. Command-line arguments
 * outrank {@code application.yml} instead.
 */
public abstract class CrossModuleTestSupport {

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    private static ConfigurableApplicationContext authorizationServerContext;
    private static ConfigurableApplicationContext resourceServerContext;

    @BeforeAll
    static void startServers() {
        if (authorizationServerContext != null) {
            return;
        }
        postgres.start();

        authorizationServerContext = new SpringApplicationBuilder(AuthorizationServerApplication.class)
                .run(
                        "--server.port=9000",
                        "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--spring.flyway.locations=classpath:db/migration/authserver");

        resourceServerContext = new SpringApplicationBuilder(ResourceServerApplication.class)
                .run(
                        "--server.port=8082",
                        "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--spring.flyway.locations=classpath:db/migration/resourceserver",
                        "--spring.flyway.schemas=resource_server",
                        "--spring.flyway.default-schema=resource_server",
                        "--spring.jpa.properties.hibernate.default_schema=resource_server",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--spring.jpa.open-in-view=false",
                        "--spring.mvc.problemdetails.enabled=true",
                        "--spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000",
                        "--app.security.expected-audience=expense-tracker-api");
    }

    @AfterAll
    static void stopServers() {
        // Left running: only one IT class uses this base today, and Ryuk
        // reaps the container on JVM exit regardless (same reasoning as
        // authorization-server's AbstractIntegrationTest).
    }
}
