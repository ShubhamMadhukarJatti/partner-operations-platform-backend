package com.sharkdom.reseller.repository;

import com.sharkdom.entity.stripe.StripeEvent;
import com.sharkdom.reseller.entity.ResellerStripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResellerStripeEventRepository extends JpaRepository<ResellerStripeEvent, Long> {

    List<ResellerStripeEvent> findAllByEventId(String eventId);

}
