package com.mel.expensetracker.resourceserver.report;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * [FEATURE C4] Custom {@code AuthorizationManager} for org-tenancy and
 * ownership on the {@code /reports/{id}} family. Constructed twice in
 * {@code SecurityConfig}: tenancy-only for approve/delete (a manager or
 * org-admin isn't the submitter), tenancy+ownership for GET/PUT/add-item
 * (submitter only).
 *
 * <p>A missing report grants access deliberately -- the service layer's own
 * lookup then throws {@link ReportNotFoundException}, giving a 404, not a
 * 403. Conflating "doesn't exist" with "not yours" here would mean acceptance
 * criterion 3's cross-org 403 could never be told apart from a plain typo in
 * the id.
 *
 * <p>A client_credentials (M2M) token carries no {@code org} claim at all, so
 * the org comparison below fails it by construction -- no separate check
 * needed to keep {@code export-worker} off these endpoints.
 */
public class ReportAccessAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final ExpenseReportRepository reportRepository;
    private final boolean requireOwnership;

    public ReportAccessAuthorizationManager(ExpenseReportRepository reportRepository, boolean requireOwnership) {
        this.reportRepository = reportRepository;
        this.requireOwnership = requireOwnership;
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        UUID id = UUID.fromString(context.getVariables().get("id"));
        return reportRepository
                .findById(id)
                .map(report -> new AuthorizationDecision(isAllowed(report, authentication.get())))
                .orElseGet(() -> new AuthorizationDecision(true));
    }

    private boolean isAllowed(ExpenseReport report, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        if (!report.getOrgSlug().equals(jwt.getClaimAsString("org"))) {
            return false;
        }
        return !requireOwnership || report.getSubmitterSubject().equals(jwt.getSubject());
    }
}
