package com.mel.expensetracker.bff.relay.dto;

/** Mirrors resource-server's {@code ExpenseReportStatus} on the wire. */
public enum ExpenseReportStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED
}
