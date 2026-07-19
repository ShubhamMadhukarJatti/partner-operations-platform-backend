package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.PriceEntity;
import com.sharkdom.model.stripe.PriceEntityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StripePriceMapper {

    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "lineItems", ignore = true)
    PriceEntityDto priceEntityToPriceEntityDto(PriceEntity priceEntity);

    PriceEntity priceEntityDtoToPriceEntity(PriceEntityDto priceEntity);

}
