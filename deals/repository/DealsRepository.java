package com.sharkdom.deals.repository;

import com.sharkdom.deals.entity.DealsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DealsRepository extends JpaRepository<DealsEntity, Long> {
    List<DealsEntity> findAllByOrganizationId(Long organizationId);

    @Query("select d from DealsEntity d where d.organizationId != :organizationId")
    List<DealsEntity> findAllExceptMyDeals(Long organizationId);

    DealsEntity findByDealId(String dealId);

    List<DealsEntity> findAllByDealIdIn(Set<String> joinedDealIds);
}
