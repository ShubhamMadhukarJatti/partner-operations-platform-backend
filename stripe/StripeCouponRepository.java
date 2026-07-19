package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeCouponRepository extends JpaRepository<StripeCoupon, Long> {

    boolean existsByCouponId(String couponId);
    Optional<StripeCoupon> findByCouponId(String couponId);
}
