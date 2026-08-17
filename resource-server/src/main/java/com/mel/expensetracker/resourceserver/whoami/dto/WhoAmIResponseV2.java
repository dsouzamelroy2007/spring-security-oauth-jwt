package com.mel.expensetracker.resourceserver.whoami.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Richer than v1: exposes the resolved {@code roles} claim distinctly from the
 * derived {@code authorities}, plus the raw claim map -- the single most
 * useful endpoint for showing what the framework actually did with a token.
 */
public record WhoAmIResponseV2(
        String subject, String orgSlug, Set<String> roles, List<String> authorities, Map<String, Object> claims) {}
