package com.mel.expensetracker.resourceserver.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * [style: collection filtering] GET /api/v1/reports is tenant-scoped by the
 * query, then {@code @PostFilter} drops other users' drafts from the result.
 *
 * <p>Known, accepted caveat: {@code @PostFilter} runs after pagination, so a
 * page can come back smaller than its requested size once someone else's
 * drafts are dropped -- fine for a portfolio-scale demo dataset, called out
 * explicitly rather than silently accepted.
 */
class ExpenseReportPostFilterTest extends AbstractIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @SuppressWarnings("unchecked")
    void bobDoesNotSeeAlicesDraftButSeesNonDraftAcmeReports() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        List<Map<String, Object>> reports = client()
                .get()
                .uri("/api/v1/reports")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        List<String> titles = reports.stream().map(r -> (String) r.get("title")).toList();
        assertThat(titles).contains("Q1 client travel", "Team offsite");
        assertThat(titles).doesNotContain("Conference trip planning");
    }

    @Test
    @SuppressWarnings("unchecked")
    void aliceSeesHerOwnDraftInTheList() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        List<Map<String, Object>> reports = client()
                .get()
                .uri("/api/v1/reports")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        List<String> titles = reports.stream().map(r -> (String) r.get("title")).toList();
        assertThat(titles).contains("Conference trip planning", "Q1 client travel", "Team offsite");
    }
}
