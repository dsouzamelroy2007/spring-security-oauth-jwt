package com.mel.expensetracker.shared.authz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents, in code, that a {@code @RestController} method is deliberately
 * public -- no runtime effect. Exists so ArchUnit's "no controller method
 * escapes an authorization check" rule has a real allowlist mechanism instead
 * of a loophole: a method is either annotated with a real authorization check
 * or explicitly marked public, never silently neither.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {}
