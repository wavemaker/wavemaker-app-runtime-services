package com.wavemaker.runtime.constraints.constraintvalidators;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.wavemaker.runtime.constraints.WMPast;

/**
 * @author : Sanjana Raheja
 * @since : 01/10/20
 */
public class WMPastValidatorForDate implements ConstraintValidator<WMPast, Date> {

    @Override
    public void initialize(WMPast constraintAnnotation) {
    }

    @Override
    public boolean isValid(Date inputDate, ConstraintValidatorContext context) {
        Date currentDate = new Date();
        if (inputDate.before(currentDate)) {
            return true;
        } else {
            return false;
        }
    }
}
