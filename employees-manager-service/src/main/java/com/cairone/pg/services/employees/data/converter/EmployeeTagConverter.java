package com.cairone.pg.services.employees.data.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.pg.services.employees.data.enums.EmployeeTag;

@Converter(autoApply = true)
public class EmployeeTagConverter implements AttributeConverter<EmployeeTag, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EmployeeTag attribute) {
        return attribute.getDbValue();
    }

    @Override
    public EmployeeTag convertToEntityAttribute(Integer dbData) {
        return EmployeeTag.of(dbData);
    }
}
