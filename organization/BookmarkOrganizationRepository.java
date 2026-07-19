package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.BookmarkOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkOrganizationRepository extends JpaRepository<BookmarkOrganization, Long> {
    Optional<BookmarkOrganization> findByOrganizationId(Long organizationId);
}
