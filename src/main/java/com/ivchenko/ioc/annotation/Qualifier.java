package com.ivchenko.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Component constructor parameters should use this annotation to determine which implementation should be used
 * Otherwise first found implementation will be used
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Qualifier {
    /**
     * @return simple name of implementation class ({@link Class#getSimpleName()})
     */
    String value();
}
