package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.OrganizationFollowerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationFollowerHistoryRepository extends JpaRepository<OrganizationFollowerHistory, Long> {

}
