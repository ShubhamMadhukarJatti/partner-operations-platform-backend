package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.EmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgEmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
    @Query(value = "SELECT senderOrganizationId from EmailHistory where organizationId = :organizationId")
    List<Long> findAllByOrganizationId(Long organizationId);
}
