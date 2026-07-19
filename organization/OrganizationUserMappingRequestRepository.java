package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.OrganizationUserMappingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationUserMappingRequestRepository extends JpaRepository<OrganizationUserMappingRequest, Long> {

    Optional<OrganizationUserMappingRequest> findOneByRequestId(String requestId);

}