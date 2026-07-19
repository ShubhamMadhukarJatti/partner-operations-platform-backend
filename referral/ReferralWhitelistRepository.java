package com.sharkdom.repository.referral;

import com.sharkdom.entity.referral.ReferralWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralWhitelistRepository extends JpaRepository<ReferralWhitelist, Long> {
    Optional<ReferralWhitelist> findByDomain(String domain);

    @Query(value = "select domain from ReferralWhitelist")
    List<String> findAllDomains();
}
