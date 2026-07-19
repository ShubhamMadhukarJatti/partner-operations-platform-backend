package com.sharkdom.gtm.repository;

import com.sharkdom.gtm.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByExternalPartnerIdAndOrganizationId(
            Long externalPartnerId,
            Long organizationId,
            Pageable pageable
    );

    Page<Task> findByExternalPartnerCodeAndOrganizationId(
            String externalPartnerCode,
            Long organizationId,
            Pageable pageable
    );

}