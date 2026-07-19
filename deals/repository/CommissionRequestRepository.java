package com.sharkdom.deals.repository;

import com.sharkdom.deals.entity.CommissionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionRequestRepository extends JpaRepository<CommissionRequestEntity, Long> {

    List<CommissionRequestEntity> findByOrganizationId(Long organizationId);
}
