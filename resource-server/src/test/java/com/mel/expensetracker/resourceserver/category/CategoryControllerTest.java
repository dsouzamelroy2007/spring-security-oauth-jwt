package com.mel.expensetracker.resourceserver.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CategoryControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @SuppressWarnings("unchecked")
    void isReachableWithoutAuthentication() {
        List<Map<String, Object>> body = client()
                .get()
                .uri("/api/v1/categories")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotEmpty();
        List<Object> names = body.stream().map(m -> m.get("name")).toList();
        assertThat(names).contains("Travel", "Meals", "Software");
    }
}
