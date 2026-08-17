package com.mel.expensetracker.resourceserver.report;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * [FEATURE C4] The centerpiece: every endpoint style from SPEC's map, driven
 * table-style across every seeded principal plus anonymous plus the M2M
 * client, asserting the exact HTTP status. Deliberately fails loudly if any
 * endpoint's {@code SecurityConfig}/{@code @PreAuthorize}/{@code @IsOrgAdmin}
 * rule is loosened by hand.
 *
 * <p>Mutating endpoints (approve/delete) only cover principals that are
 * structurally denied by the seed data (nobody seeded is both acme-org and
 * ORG_ADMIN, so {@code e3333333} can never actually be deleted here; the
 * globex-only approve rows never include a globex MANAGER-or-above
 * principal). The one-time positive case for each is proven separately in
 * {@link ExpenseReportApprovalTest} and {@link ExpenseReportDeleteAuthorizationTest}
 * against dedicated fixtures -- repeating a real mutation here, across a
 * shared long-lived Testcontainers Postgres, would make this test's outcome
 * depend on run order.
 */
class ExpenseReportAuthorizationMatrixTest extends AbstractIntegrationTest {

    // acme: alice's own SUBMITTED report.
    private static final String ACME_OWNED_BY_ALICE = "e2222222-2222-2222-2222-222222222222";
    // acme: bob's own APPROVED report -- nobody seeded is acme+ORG_ADMIN, so
    // this can never actually be deleted by any principal tested here.
    private static final String ACME_OWNED_BY_BOB = "e3333333-3333-3333-3333-333333333333";
    // globex: carol's own APPROVED report -- only globex MANAGER-or-above
    // (carol, dana) could actually approve it; both excluded from this table.
    private static final String GLOBEX_OWNED_BY_CAROL = "e4444444-4444-4444-4444-444444444444";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    static Stream<Arguments> cases() {
        return Stream.of(
                // -- public --
                row("GET /categories, anonymous", HttpMethod.GET, "/api/v1/categories", null, 200),
                row("GET /categories, alice", HttpMethod.GET, "/api/v1/categories", TestJwtSupport::alice, 200),
                row("GET /categories, export-worker", HttpMethod.GET, "/api/v1/categories", TestJwtSupport::exportWorker, 200),

                // -- authenticated, any role --
                row("GET /whoami, anonymous", HttpMethod.GET, "/api/v1/whoami", null, 401),
                row("GET /whoami, alice", HttpMethod.GET, "/api/v1/whoami", TestJwtSupport::alice, 200),
                row("GET /whoami, bob", HttpMethod.GET, "/api/v1/whoami", TestJwtSupport::bob, 200),
                row("GET /whoami, carol", HttpMethod.GET, "/api/v1/whoami", TestJwtSupport::carol, 200),
                row("GET /whoami, dana", HttpMethod.GET, "/api/v1/whoami", TestJwtSupport::dana, 200),
                row("GET /whoami, export-worker", HttpMethod.GET, "/api/v1/whoami", TestJwtSupport::exportWorker, 200),

                // -- scope-gated M2M --
                row("GET /reports/export, anonymous", HttpMethod.GET, "/api/v1/reports/export", null, 401),
                row("GET /reports/export, alice", HttpMethod.GET, "/api/v1/reports/export", TestJwtSupport::alice, 403),
                row("GET /reports/export, bob", HttpMethod.GET, "/api/v1/reports/export", TestJwtSupport::bob, 403),
                row("GET /reports/export, export-worker", HttpMethod.GET, "/api/v1/reports/export", TestJwtSupport::exportWorker, 200),

                // -- tenant-scoped list: reports --
                row("GET /reports, anonymous", HttpMethod.GET, "/api/v1/reports", null, 401),
                row("GET /reports, alice", HttpMethod.GET, "/api/v1/reports", TestJwtSupport::alice, 200),
                row("GET /reports, bob", HttpMethod.GET, "/api/v1/reports", TestJwtSupport::bob, 200),
                row("GET /reports, carol", HttpMethod.GET, "/api/v1/reports", TestJwtSupport::carol, 200),
                row("GET /reports, dana", HttpMethod.GET, "/api/v1/reports", TestJwtSupport::dana, 200),

                // -- tenant-scoped list: reimbursements (field redaction happens
                //    within a 200, not a status change -- see ReimbursementRedactionTest) --
                row("GET /reimbursements, anonymous", HttpMethod.GET, "/api/v1/reimbursements", null, 401),
                row("GET /reimbursements, alice", HttpMethod.GET, "/api/v1/reimbursements", TestJwtSupport::alice, 200),
                row("GET /reimbursements, carol", HttpMethod.GET, "/api/v1/reimbursements", TestJwtSupport::carol, 200),

                // -- owner-only: GET /reports/{id}, alice's report --
                row("GET /reports/{id}, anonymous", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, null, 401),
                row("GET /reports/{id}, owner (alice)", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, TestJwtSupport::alice, 200),
                row("GET /reports/{id}, same-org non-owner (bob)", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, TestJwtSupport::bob, 403),
                row("GET /reports/{id}, cross-org (carol)", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, TestJwtSupport::carol, 403),
                row("GET /reports/{id}, cross-org (dana)", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, TestJwtSupport::dana, 403),
                row("GET /reports/{id}, M2M (export-worker)", HttpMethod.GET, "/api/v1/reports/" + ACME_OWNED_BY_ALICE, TestJwtSupport::exportWorker, 403),

                // -- role-gated: POST /reports/{id}/approve, denied principals only --
                row("POST /reports/{id}/approve, anonymous", HttpMethod.POST, "/api/v1/reports/" + GLOBEX_OWNED_BY_CAROL + "/approve", null, 401),
                row("POST /reports/{id}/approve, cross-org acme employee (alice)", HttpMethod.POST, "/api/v1/reports/" + GLOBEX_OWNED_BY_CAROL + "/approve", TestJwtSupport::alice, 403),
                row("POST /reports/{id}/approve, cross-org acme manager (bob)", HttpMethod.POST, "/api/v1/reports/" + GLOBEX_OWNED_BY_CAROL + "/approve", TestJwtSupport::bob, 403),

                // -- meta-annotation: DELETE /reports/{id}, denied principals only --
                row("DELETE /reports/{id}, anonymous", HttpMethod.DELETE, "/api/v1/reports/" + ACME_OWNED_BY_BOB, null, 401),
                row("DELETE /reports/{id}, same-org employee (alice)", HttpMethod.DELETE, "/api/v1/reports/" + ACME_OWNED_BY_BOB, TestJwtSupport::alice, 403),
                row("DELETE /reports/{id}, owner but not org-admin (bob)", HttpMethod.DELETE, "/api/v1/reports/" + ACME_OWNED_BY_BOB, TestJwtSupport::bob, 403),
                row("DELETE /reports/{id}, cross-org org-admin (dana)", HttpMethod.DELETE, "/api/v1/reports/" + ACME_OWNED_BY_BOB, TestJwtSupport::dana, 403));
    }

    private static Arguments row(String description, HttpMethod method, String uri, Supplier<Jwt> principal, int expectedStatus) {
        return Arguments.of(description, method, uri, principal, expectedStatus);
    }

    @ParameterizedTest(name = "{0} -> {4}")
    @MethodSource("cases")
    void matrix(String description, HttpMethod method, String uri, Supplier<Jwt> principal, int expectedStatus) {
        if (principal != null) {
            when(jwtDecoder.decode(anyString())).thenReturn(principal.get());
        }

        var request = client().method(method).uri(uri);
        if (principal != null) {
            request = request.header("Authorization", "Bearer placeholder");
        }

        request.exchange().expectStatus().isEqualTo(expectedStatus);
    }
}
