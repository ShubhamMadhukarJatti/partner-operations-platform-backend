package com.sharkdom.mypartner.repository;

import com.sharkdom.mypartner.entity.MyPartnerSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyPartnerSegmentRepository extends JpaRepository<MyPartnerSegment,Long>
{
    Optional<MyPartnerSegment> findByOrganizationIdAndSegmentName(Long organizationId, String segmentName);

    Page<MyPartnerSegment> findAllByOrganizationId(Long organizationId, Pageable pageable);

    Optional<MyPartnerSegment> findByIdAndOrganizationId(Long id, Long organizationId);
}

