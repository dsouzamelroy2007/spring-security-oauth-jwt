package com.mel.expensetracker.resourceserver.report.dto;

import com.mel.expensetracker.resourceserver.item.dto.ExpenseItemResponse;
import com.mel.expensetracker.resourceserver.report.ExpenseReport;
import com.mel.expensetracker.resourceserver.report.ExpenseReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExpenseReportDetail(
        UUID id,
        ExpenseReportStatus status,
        String title,
        String description,
        String currency,
        BigDecimal totalAmount,
        String submitterSubject,
        long version,
        Instant createdAt,
        Instant updatedAt,
        List<ExpenseItemResponse> items) {

    public static ExpenseReportDetail from(ExpenseReport report) {
        return new ExpenseReportDetail(
                report.getId(),
                report.getStatus(),
                report.getTitle(),
                report.getDescription(),
                report.getCurrency(),
                report.getTotalAmount(),
                report.getSubmitterSubject(),
                report.getVersion(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getItems().stream().map(ExpenseItemResponse::from).toList());
    }
}
