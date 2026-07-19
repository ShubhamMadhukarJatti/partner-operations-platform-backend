package com.sharkdom.service.stripe;

import com.sharkdom.model.stripe.StripeCouponDto;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface StripeCouponService {

    StripeCouponDto createStripeCoupon(String couponName, BigDecimal percentOff) throws StripeException;

    List<StripeCouponDto> getAllCoupons() throws StripeException;

    StripeCouponDto getStripeCouponByCouponId(String couponId) throws StripeException;

    StripeCouponDto deleteStripeCouponByCouponId(String couponId) throws StripeException;

}
