package com.mel.expensetracker.resourceserver.report;

/** Thrown when an {@code If-Match} precondition doesn't match the entity's current {@code @Version}. */
public class VersionConflictException extends RuntimeException {

    public VersionConflictException(long expected, long actual) {
        super("If-Match version " + expected + " does not match the current version " + actual);
    }
}
