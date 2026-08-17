package com.mel.expensetracker.bff.relay.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Mirrors resource-server's {@code ExpenseReportSummary} (the list-view shape, no items). */
public record ExpenseReportSummary(
        UUID id,
        ExpenseReportStatus status,
        String title,
        String currency,
        BigDecimal totalAmount,
        String submitterSubject,
        long version,
        Instant createdAt,
        Instant updatedAt) {}
