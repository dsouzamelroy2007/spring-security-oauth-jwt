package com.mel.expensetracker.shared.authz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * [FEATURE C3] Meta-annotation over {@code @PreAuthorize}: the org-admin check
 * appears the same way everywhere it's used (currently: report deletion) and
 * reads as a business rule at the call site instead of a SpEL expression.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ORG_ADMIN')")
public @interface IsOrgAdmin {}
