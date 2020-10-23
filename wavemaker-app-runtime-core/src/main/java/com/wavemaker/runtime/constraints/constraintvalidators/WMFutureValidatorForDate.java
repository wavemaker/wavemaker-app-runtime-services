package com.wavemaker.runtime.constraints.constraintvalidators;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.wavemaker.runtime.constraints.WMFuture;

/**
 * @author : Sanjana Raheja
 * @since : 01/10/20
 */
public class WMFutureValidatorForDate implements ConstraintValidator<WMFuture, Date> {

    private Date currentDate;

    @Override
    public void initialize(WMFuture constraintAnnotation) {
        this.currentDate = new Date();
    }

    @Override
    public boolean isValid(Date inputDate, ConstraintValidatorContext context) {
        if (inputDate.after(currentDate)) {
            return true;
        } else {
            return false;
        }
    }
}
