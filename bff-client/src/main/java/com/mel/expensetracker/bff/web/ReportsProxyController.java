package com.mel.expensetracker.bff.web;

import com.mel.expensetracker.bff.relay.ResourceServerClient;
import com.mel.expensetracker.bff.relay.dto.CreateExpenseReportRequest;
import com.mel.expensetracker.bff.relay.dto.ExpenseReportDetail;
import com.mel.expensetracker.bff.relay.dto.ExpenseReportSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * [FEATURE B1] SPA-facing report endpoints, relayed to resource-server's
 * {@code /api/v1/reports/*} as the signed-in user. Deliberately its own
 * unversioned {@code /api/reports} contract, not a mirror of
 * resource-server's versioned path -- the BFF's API surface is the SPA's own
 * contract, not a passthrough of resource-server's.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportsProxyController {

    private final ResourceServerClient resourceServerClient;

    public ReportsProxyController(ResourceServerClient resourceServerClient) {
        this.resourceServerClient = resourceServerClient;
    }

    @GetMapping
    public List<ExpenseReportSummary> list() {
        return resourceServerClient.listReports();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseReportDetail create(@RequestBody CreateExpenseReportRequest request) {
        return resourceServerClient.createReport(request);
    }

    @PostMapping("/{id}/approve")
    public ExpenseReportDetail approve(@PathVariable UUID id) {
        return resourceServerClient.approveReport(id);
    }
}
