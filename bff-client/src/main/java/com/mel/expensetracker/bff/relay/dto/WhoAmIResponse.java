package com.mel.expensetracker.bff.relay.dto;

import java.util.List;

/** Mirrors resource-server's {@code WhoAmIResponseV1} (the default, unversioned call). */
public record WhoAmIResponse(String subject, String orgSlug, List<String> authorities) {}
