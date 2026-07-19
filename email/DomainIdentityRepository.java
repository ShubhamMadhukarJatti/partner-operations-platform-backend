package com.sharkdom.repository.email;

import com.sharkdom.entity.email.DomainIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainIdentityRepository extends JpaRepository<DomainIdentity, Long> {
    Optional<DomainIdentity> findByOrganizationId(Long organizationId);
    @Query("SELECT d FROM DomainIdentity d WHERE d.isVerified = false")
    List<DomainIdentity> findAllPendingVerification();
}
