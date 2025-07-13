package com.osmerion.atbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO document usage, handling of nullable, omittable

/**
 * This annotation may be applied to a record to instruct the AtBuilder annotation processor to generate a builder for
 * the record.
 *
 * <p></p>
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Builder {}
