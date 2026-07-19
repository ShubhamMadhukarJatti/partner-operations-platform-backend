package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.ReceiverBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ReceiverBenefitRepository extends JpaRepository<ReceiverBenefit, Long> {
}
