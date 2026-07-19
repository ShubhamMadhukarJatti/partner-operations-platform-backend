package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.SenderBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SenderBenefitRepository extends JpaRepository<SenderBenefit, Long> {
}
