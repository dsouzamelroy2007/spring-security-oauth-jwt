package com.mel.expensetracker.bff.relay;

import com.mel.expensetracker.bff.relay.dto.CreateExpenseReportRequest;
import com.mel.expensetracker.bff.relay.dto.ExpenseReportDetail;
import com.mel.expensetracker.bff.relay.dto.ExpenseReportSummary;
import com.mel.expensetracker.bff.relay.dto.WhoAmIResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * [FEATURE B1] Declarative relay to resource-server (SPEC's "one @HttpExchange
 * declarative client... with token relay"). Every call rides on the
 * {@code OAuth2ClientHttpRequestInterceptor} wired in {@code RelayClientConfig},
 * which attaches -- and, per B3, transparently refreshes -- the signed-in
 * user's own bearer token. There is no service-account credential here: the
 * BFF always calls resource-server strictly as the current caller.
 */
public interface ResourceServerClient {

    @GetExchange("/api/v1/whoami")
    WhoAmIResponse whoAmI();

    @GetExchange("/api/v1/reports")
    List<ExpenseReportSummary> listReports();

    @PostExchange("/api/v1/reports")
    ExpenseReportDetail createReport(@RequestBody CreateExpenseReportRequest request);

    @PostExchange("/api/v1/reports/{id}/approve")
    ExpenseReportDetail approveReport(@PathVariable UUID id);
}
