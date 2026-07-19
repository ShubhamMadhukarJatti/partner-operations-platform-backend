package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.stripe.StripeCoupon;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.mapper.stripe.StripeCouponMapper;
import com.sharkdom.model.stripe.StripeCouponDto;
import com.sharkdom.repository.stripe.StripeCouponRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeCouponServiceImpl implements StripeCouponService {

    private final StripeCouponRepository stripeCouponRepository;

    private final StripeCouponMapper stripeCouponMapper;

    private final StripeService stripeService;

    @Override
    @Transactional
    public StripeCouponDto createStripeCoupon(String couponName, BigDecimal percentOff) throws StripeException {
        Coupon createdCoupon = stripeService.createStripeCoupon(couponName, percentOff);
        Coupon retreiveCoupon = stripeService.retreiveStripeCoupon(createdCoupon.getId());
        StripeCoupon mappedStripeCoupon = mapStripeCoupon(retreiveCoupon);
        StripeCoupon savedStripeCoupon = stripeCouponRepository.save(mappedStripeCoupon);
        return stripeCouponMapper.stripeCouponToStripeCouponDto(savedStripeCoupon);
    }

    @Override
    @Transactional
    public StripeCouponDto getStripeCouponByCouponId(String couponId) throws StripeException {
        Coupon retreiveCoupon = stripeService.retreiveStripeCoupon(couponId);
        StripeCoupon mappedStripeCoupon = mapStripeCoupon(retreiveCoupon);
        if (stripeCouponRepository.existsByCouponId(couponId)) {
            return stripeCouponMapper.stripeCouponToStripeCouponDto(mappedStripeCoupon);
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH138, couponId);
        }
    }

    @Override
    @Transactional
    public StripeCouponDto deleteStripeCouponByCouponId(String couponId) throws StripeException {
        Coupon deletedCoupon = stripeService.deleteStripeCoupon(couponId);
        StripeCoupon coupon = stripeCouponRepository.findByCouponId(couponId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH138, couponId));
        coupon.setDeleted(deletedCoupon.getDeleted());
        StripeCoupon savedDeleteCoupon = stripeCouponRepository.save(coupon);
        return stripeCouponMapper.stripeCouponToStripeCouponDto(savedDeleteCoupon);
    }

    @Override
    @Transactional
    public List<StripeCouponDto> getAllCoupons() throws StripeException {
        List<Coupon> stripeCoupons = stripeService.retrieveAllStripeCoupon();
        return stripeCoupons.stream()
                .map(this::mapStripeCoupon)
                .filter(coupon -> stripeCouponRepository.existsByCouponId(coupon.getCouponId()))
                .map(stripeCouponMapper::stripeCouponToStripeCouponDto)
                .toList();
    }

    private StripeCoupon mapStripeCoupon(Coupon retreiveCoupon) {
        return StripeCoupon.builder()
                .couponName(retreiveCoupon.getName())
                .couponId(retreiveCoupon.getId())
                .percentOff(retreiveCoupon.getPercentOff())
                .isDeleted(false)
                .build();
    }

}
