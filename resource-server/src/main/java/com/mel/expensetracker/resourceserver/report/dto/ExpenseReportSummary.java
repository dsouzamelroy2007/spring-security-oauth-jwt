package com.mel.expensetracker.resourceserver.report.dto;

import com.mel.expensetracker.resourceserver.report.ExpenseReport;
import com.mel.expensetracker.resourceserver.report.ExpenseReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExpenseReportSummary(
        UUID id,
        ExpenseReportStatus status,
        String title,
        String currency,
        BigDecimal totalAmount,
        String submitterSubject,
        long version,
        Instant createdAt,
        Instant updatedAt) {

    public static ExpenseReportSummary from(ExpenseReport report) {
        return new ExpenseReportSummary(
                report.getId(),
                report.getStatus(),
                report.getTitle(),
                report.getCurrency(),
                report.getTotalAmount(),
                report.getSubmitterSubject(),
                report.getVersion(),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }
}
