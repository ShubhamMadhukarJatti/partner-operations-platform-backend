package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.ReferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<ReferralEntity, Long> {
    @Query(value = "select organizationId from ReferralEntity where referralCode=:referralCode")
    Long existByReferralCode(String referralCode);

    List<ReferralEntity> findByOrganizationId(Long organizationId);
}
