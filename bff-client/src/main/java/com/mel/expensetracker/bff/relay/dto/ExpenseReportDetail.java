package com.mel.expensetracker.bff.relay.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Mirrors resource-server's {@code ExpenseReportDetail} (create/get/update/approve/addItem shape). */
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
        List<ExpenseItemResponse> items) {}
