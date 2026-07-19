package com.sharkdom.requirement.repository;

import com.sharkdom.requirement.entity.CommunityOptIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityOptInRepository extends JpaRepository<CommunityOptIn, Long> {

    boolean existsByContactEmailIgnoreCase(String contactEmail);
}
