package com.mel.expensetracker.bff.security;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/** [FEATURE D1] CORS with a working preflight for the one genuinely cross-origin case. */
class CorsPreflightTest extends AbstractBffIntegrationTest {

    @Test
    void preflightFromTheAllowedOriginIsGrantedAccessControlHeaders() {
        client().options()
                .uri("/api/reports")
                .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5500");
    }

    @Test
    void preflightFromAnUnlistedOriginGetsNoAccessControlHeaders() {
        client().options()
                .uri("/api/reports")
                .header(HttpHeaders.ORIGIN, "https://attacker.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectHeader()
                .doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }
}
