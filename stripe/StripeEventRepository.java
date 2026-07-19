package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StripeEventRepository extends JpaRepository<StripeEvent, Long> {

    List<StripeEvent> findAllByEventId(String eventId);

    boolean existsByEventId(String eventId);

}
