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

    private Date currentDate;

    @Override
    public void initialize(WMPast constraintAnnotation) {
        currentDate = new Date();
    }

    @Override
    public boolean isValid(Date inputDate, ConstraintValidatorContext context) {
        if (inputDate.before(currentDate)) {
            return true;
        } else {
            return false;
        }
    }
}
