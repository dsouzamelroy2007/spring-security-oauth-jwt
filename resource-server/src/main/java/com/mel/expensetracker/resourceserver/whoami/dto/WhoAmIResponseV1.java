package com.mel.expensetracker.resourceserver.whoami.dto;

import java.util.List;

public record WhoAmIResponseV1(String subject, String orgSlug, List<String> authorities) {}
