package com.mel.expensetracker.bff.relay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Mirrors resource-server's {@code ExpenseItemResponse}. */
public record ExpenseItemResponse(
        UUID id, UUID categoryId, String categoryName, BigDecimal amount, String currency, LocalDate expenseDate, String note) {}
