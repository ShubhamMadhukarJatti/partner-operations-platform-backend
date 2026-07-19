package com.sharkdom.converter;

import com.sharkdom.converter.AbstractEnumListConverter;
import com.sharkdom.entity.organization.RegionToPartnerWith;
import jakarta.persistence.Converter;

@Converter
public class RegionConverter extends AbstractEnumListConverter<RegionToPartnerWith> {
    public RegionConverter() {
        super(RegionToPartnerWith.class);
    }
}