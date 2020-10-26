package com.wavemaker.runtime.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.wavemaker.runtime.constraints.constraintvalidators.WMPastValidatorForDate;

/**
 * @author : Sanjana Raheja
 * @since : 01/10/20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = {WMPastValidatorForDate.class})
public @interface WMPast {

    String message() default "must be in past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
