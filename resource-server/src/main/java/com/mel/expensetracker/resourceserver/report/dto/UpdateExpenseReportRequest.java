package com.mel.expensetracker.resourceserver.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateExpenseReportRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotBlank @Size(min = 3, max = 3) String currency) {}
