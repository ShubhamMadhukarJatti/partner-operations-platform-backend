package com.sharkdom.converter;

import com.sharkdom.entity.organization.ServingCustomersType;
import jakarta.persistence.Converter;

@Converter
public class ServingCustomersTypeListConverter extends AbstractEnumListConverter<ServingCustomersType> {
    public ServingCustomersTypeListConverter() {
        super(ServingCustomersType.class);
    }
}