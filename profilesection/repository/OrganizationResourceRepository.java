package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationResourceRepository extends JpaRepository<OrganizationResource, Long> {


    List<OrganizationResource> findByOrganizationId(Long organizationId);

}