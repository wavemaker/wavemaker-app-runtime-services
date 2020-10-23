package com.wavemaker.runtime.constraints;

/**
 * @author : Sanjana Raheja
 * @since : 01/10/20
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.wavemaker.runtime.constraints.constraintvalidators.WMFutureValidatorForDate;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = {WMFutureValidatorForDate.class})
public @interface WMFuture {

    String message() default "Date must be in future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
