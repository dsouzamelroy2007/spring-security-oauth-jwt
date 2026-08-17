package com.mel.expensetracker.resourceserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ResourceServerApplicationTests {

    // JwtDecoderConfig's real bean does issuer discovery against
    // authorization-server at context-refresh time -- not appropriate for a
    // plain "does the context start" smoke test, which shouldn't depend on a
    // second service being up.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }
}
