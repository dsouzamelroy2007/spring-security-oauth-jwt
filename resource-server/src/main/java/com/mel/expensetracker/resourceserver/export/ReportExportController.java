package com.mel.expensetracker.resourceserver.export;

import com.mel.expensetracker.resourceserver.report.ExpenseReportRepository;
import com.mel.expensetracker.resourceserver.report.dto.ExpenseReportSummary;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [style: scope-gated M2M] The {@code export-worker} client_credentials token
 * carries no {@code org} claim -- unlike every other report endpoint, this
 * one is deliberately not org-scoped: an export job runs across all tenants.
 */
@RestController
public class ReportExportController {

    private final ExpenseReportRepository reportRepository;

    public ReportExportController(ExpenseReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // [FEATURE B2] client_credentials-issued token, scope-gated -- no user, no org claim.
    @GetMapping("/api/v1/reports/export")
    @PreAuthorize("hasAuthority('SCOPE_expenses.export')")
    public List<ExpenseReportSummary> exportReports(Pageable pageable) {
        return reportRepository.findAll(pageable).getContent().stream()
                .map(ExpenseReportSummary::from)
                .toList();
    }
}
