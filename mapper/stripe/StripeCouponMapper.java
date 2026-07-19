package com.sharkdom.mapper.stripe;


import com.sharkdom.entity.stripe.StripeCoupon;
import com.sharkdom.model.stripe.StripeCouponDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {})
public interface StripeCouponMapper {

    StripeCoupon stripeCouponDtoToStripeCoupon(StripeCouponDto stripeCouponDto);

    List<StripeCoupon> stripeCouponDtoListToStripeCouponList(List<StripeCouponDto> stripeCouponDto);

    @Mapping(target = "checkoutSession", ignore = true)
    StripeCouponDto stripeCouponToStripeCouponDto(StripeCoupon stripeCoupon);


    @Mapping(target = "checkoutSession", ignore = true)
    List<StripeCouponDto> stripeCouponListToStripeCouponDtoList(List<StripeCoupon> stripeCoupons);

}
