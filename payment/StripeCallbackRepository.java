package com.sharkdom.repository.payment;

import com.sharkdom.entity.payment.StripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeCallbackRepository extends JpaRepository<StripEntity, Long> {

}
