package com.train.backend.security.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for requiring any one of multiple roles.
 * Usage: @RequireAnyRole({"admin", "moderator"}) on controller methods.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAnyRole {
    /**
     * Any of these roles are required for access.
     */
    String[] value() default {};
}
