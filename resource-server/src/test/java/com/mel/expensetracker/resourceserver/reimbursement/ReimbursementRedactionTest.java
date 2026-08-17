package com.mel.expensetracker.resourceserver.reimbursement;

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

/** [style: field redaction] Backs acceptance criterion 4: masked unless the caller effectively holds ROLE_FINANCE. */
class ReimbursementRedactionTest extends AbstractIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @SuppressWarnings("unchecked")
    void nonFinanceCallerSeesAMaskedIban() {
        // bob is MANAGER in acme -- no FINANCE user exists in acme, by design.
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        List<Map<String, Object>> reimbursements = client()
                .get()
                .uri("/api/v1/reimbursements")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(reimbursements).hasSize(1);
        String iban = (String) reimbursements.get(0).get("iban");
        assertThat(iban).isNotEqualTo("GB29NWBK60161331926819").endsWith("6819").doesNotContain("NWBK");
    }

    @Test
    @SuppressWarnings("unchecked")
    void financeCallerSeesTheRealIban() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.carol());

        List<Map<String, Object>> reimbursements = client()
                .get()
                .uri("/api/v1/reimbursements")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(reimbursements).hasSize(1);
        assertThat(reimbursements.get(0).get("iban")).isEqualTo("DE89370400440532013000");
    }

    @Test
    @SuppressWarnings("unchecked")
    void orgAdminSeesTheRealIbanViaRoleHierarchy() {
        // dana is ORG_ADMIN, which implies FINANCE (MethodSecurityConfig's role hierarchy).
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.dana());

        List<Map<String, Object>> reimbursements = client()
                .get()
                .uri("/api/v1/reimbursements")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(reimbursements).hasSize(1);
        assertThat(reimbursements.get(0).get("iban")).isEqualTo("DE89370400440532013000");
    }
}
