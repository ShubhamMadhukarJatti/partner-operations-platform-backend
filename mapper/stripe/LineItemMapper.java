package com.sharkdom.mapper.stripe;

import com.sharkdom.entity.stripe.LineItemEntity;
import com.sharkdom.model.stripe.LineItemEntityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {StripePriceMapper.class})
public interface LineItemMapper {

    @Mapping(target = "checkoutSession", ignore = true)
    @Mapping(target = "price.lineItems", ignore = true)
    @Mapping(target = "price.subscriptions", ignore = true)
    LineItemEntityDto lineItemEntityToLineItemEntityDto(LineItemEntity lineItemEntity);

    LineItemEntity lineItemEntityDtoToLineItemEntity(LineItemEntityDto lineItemEntityDto);

    List<LineItemEntity> lineItemEntityDtoListToLineItemEntityList(List<LineItemEntityDto> lineItemEntityDtoList);

}
