package com.sharkdom.repository.partnermapping;

import com.sharkdom.entity.partnermapping.MyPartnerMappingReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyPartnerMappingReportStatusRepository extends JpaRepository<MyPartnerMappingReportStatus,Long> {

    List<MyPartnerMappingReportStatus> findByOrganizationId(Long id);

    List<MyPartnerMappingReportStatus> findByUserId(String userId);

    Long countByOrganizationId(Long orgIdFromToken);

    Long countByUserId(String userId);
}
