package com.mel.expensetracker.resourceserver.report;

import com.mel.expensetracker.resourceserver.item.dto.CreateExpenseItemRequest;
import com.mel.expensetracker.resourceserver.report.dto.CreateExpenseReportRequest;
import com.mel.expensetracker.resourceserver.report.dto.ExpenseReportDetail;
import com.mel.expensetracker.resourceserver.report.dto.ExpenseReportSummary;
import com.mel.expensetracker.resourceserver.report.dto.UpdateExpenseReportRequest;
import com.mel.expensetracker.shared.authz.IsOrgAdmin;
import com.mel.expensetracker.shared.org.OrgContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenancy and ownership for the {@code /{id}} endpoints are enforced upstream
 * by {@link ReportAccessAuthorizationManager}, wired into {@code SecurityConfig}
 * -- a request that reaches these methods has already passed that check, so
 * the service layer trusts the id alone rather than re-deriving org/ownership
 * here (the one gate, not two that can drift apart).
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ExpenseReportController {

    private final ExpenseReportService reportService;

    public ExpenseReportController(ExpenseReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ExpenseReportSummary> listReports(OrgContext caller, Pageable pageable) {
        return reportService.listReports(caller.orgSlug(), pageable).stream()
                .map(ExpenseReportSummary::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseReportDetail createReport(OrgContext caller, @Valid @RequestBody CreateExpenseReportRequest request) {
        return ExpenseReportDetail.from(reportService.createReport(caller, request));
    }

    @GetMapping("/{id}")
    // Ownership already enforced upstream (class comment) -- isAuthenticated()
    // here is the ArchUnit-visible belt-and-suspenders half, not the real gate.
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseReportDetail> getReport(@PathVariable UUID id) {
        ExpenseReport report = reportService.getReport(id);
        return ResponseEntity.ok().eTag(etag(report.getVersion())).body(ExpenseReportDetail.from(report));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseReportDetail> updateReport(
            @PathVariable UUID id,
            @RequestHeader("If-Match") String ifMatch,
            @Valid @RequestBody UpdateExpenseReportRequest request) {
        ExpenseReport report = reportService.updateReport(id, request, parseEtag(ifMatch));
        return ResponseEntity.ok().eTag(etag(report.getVersion())).body(ExpenseReportDetail.from(report));
    }

    @PostMapping("/{id}/approve")
    // [FEATURE C1] hasRole('MANAGER') expands through the role hierarchy
    // (MethodSecurityConfig) -- FINANCE and ORG_ADMIN can approve too, since
    // both imply MANAGER.
    @PreAuthorize("hasRole('MANAGER')")
    public ExpenseReportDetail approveReport(@PathVariable UUID id) {
        return ExpenseReportDetail.from(reportService.approveReport(id));
    }

    @DeleteMapping("/{id}")
    @IsOrgAdmin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseReportDetail addItem(@PathVariable UUID id, @Valid @RequestBody CreateExpenseItemRequest request) {
        return ExpenseReportDetail.from(reportService.addItem(
                id, request.categoryId(), request.amount(), request.currency(), request.expenseDate(), request.note()));
    }

    private static String etag(long version) {
        return "\"" + version + "\"";
    }

    private static long parseEtag(String raw) {
        return Long.parseLong(raw.replace("\"", "").trim());
    }
}
