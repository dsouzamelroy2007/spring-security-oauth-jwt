package com.mel.expensetracker.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

/**
 * [FEATURE D4] {@code HttpSessionEventPublisher} -- the classic concurrent
 * session control wiring -- listens for Servlet container session-lifecycle
 * events, which don't reliably fire for Spring Session's Redis-backed
 * sessions. {@link SpringSessionBackedSessionRegistry} reads concurrency
 * state straight out of Redis instead, via the indexed session repository
 * ({@code spring.session.data.redis.repository-type: indexed} in application.yml
 * -- the plain, non-indexed repository doesn't implement
 * {@link FindByIndexNameSessionRepository}).
 */
@Configuration
public class SessionConfig {

    @Bean
    SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry(
            FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}
