package com.sharkdom.mypartner.repository;

import com.sharkdom.mypartner.entity.MyPartnerTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyPartnerTaskRepository extends JpaRepository<MyPartnerTask, Long> {

    List<MyPartnerTask> findByOrganizationId(Long organizationId);

    List<MyPartnerTask> findByOrganizationIdAndMyPartnerId(Long organizationId,Long myPartnerId);
}