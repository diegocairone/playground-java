package com.cairone.pg.services.employees.rest.ctrl.constraint;

import com.cairone.pg.services.employees.rest.ctrl.validator.BankAccountNumberForTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BankAccountNumberForTypeValidator.class)
public @interface BankAccountNumberForTypeConstraint {

    String message() default "Provided account number is invalid for this account type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
