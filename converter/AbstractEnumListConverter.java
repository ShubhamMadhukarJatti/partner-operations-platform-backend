package com.sharkdom.converter;

import com.sharkdom.entity.organization.ServingCustomersType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractEnumListConverter<E extends Enum<E>> implements AttributeConverter<List<E>, String> {

    private final Class<E> enumClass;

    protected AbstractEnumListConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(List<E> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<E> convertToEntityAttribute(String joined) {
        if (joined == null || joined.isEmpty()) return List.of();
        return Arrays.stream(joined.split(","))
                .map(name -> Enum.valueOf(enumClass, name))
                .collect(Collectors.toList());
    }

}



