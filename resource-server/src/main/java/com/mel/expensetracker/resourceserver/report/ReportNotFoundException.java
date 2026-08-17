package com.mel.expensetracker.resourceserver.report;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(UUID id) {
        super("No expense report with id " + id);
    }
}
