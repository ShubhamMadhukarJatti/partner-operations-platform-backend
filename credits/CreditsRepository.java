package com.sharkdom.repository.credits;

import com.sharkdom.entity.credits.Credits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditsRepository extends JpaRepository<Credits, Long> {
    Credits findByOrganizationId(Long orgId);

}
