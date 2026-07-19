package com.sharkdom.reseller.repository;

import com.sharkdom.reseller.entity.ResellerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResellerPaymentRepository extends JpaRepository<ResellerPayment,Long> {
    Optional<ResellerPayment> findTopByRequestIdOrderByIdDesc(Long requestId);
}
