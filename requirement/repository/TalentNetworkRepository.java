package com.sharkdom.requirement.repository;

import com.sharkdom.requirement.entity.TalentNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TalentNetworkRepository extends JpaRepository<TalentNetwork, Long> {

    Optional<TalentNetwork> findByContactEmailIgnoreCase(String contactEmail);

    boolean existsByContactEmailIgnoreCase(String contactEmail);
}
