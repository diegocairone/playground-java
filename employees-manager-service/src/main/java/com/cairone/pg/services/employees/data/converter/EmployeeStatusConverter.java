package com.cairone.pg.services.employees.data.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.pg.services.employees.data.enums.EmployeeStatus;

@Converter(autoApply = true)
public class EmployeeStatusConverter implements AttributeConverter<EmployeeStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EmployeeStatus attribute) {
        return attribute.getDbValue();
    }

    @Override
    public EmployeeStatus convertToEntityAttribute(Integer dbData) {
        return EmployeeStatus.of(dbData);
    }
}
