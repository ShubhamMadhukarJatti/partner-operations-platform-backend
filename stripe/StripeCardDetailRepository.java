package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeCardDetailRepository extends JpaRepository<StripeCardDetail, Long> {

}
