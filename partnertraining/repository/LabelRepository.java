package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByNameIgnoreCase(String name);
    Optional<Label> findByOrganizationId(Long organizationId);
}