package com.mel.expensetracker.resourceserver.reimbursement.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReimbursementResponse(UUID id, String orgSlug, String iban, BigDecimal amount, String currency, Instant paidAt) {}
