package com.sharkdom.repository.stripe;

import com.sharkdom.entity.stripe.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripePriceRepository extends JpaRepository<PriceEntity, Long> {

    Optional<PriceEntity> findByStripePriceId(String stripePriceId);

}
