package com.mel.expensetracker.bff.relay.dto;

/**
 * Mirrors resource-server's {@code CreateExpenseReportRequest}. No Bean
 * Validation annotations here -- resource-server is the real validation
 * boundary and already returns an RFC 9457 body on a bad request; duplicating
 * the same rules here would just be two places to keep in sync.
 */
public record CreateExpenseReportRequest(String title, String description, String currency) {}
