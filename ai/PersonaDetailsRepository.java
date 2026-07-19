package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaDetailsRepository extends JpaRepository<PersonaDetailsEntity, Long> {
    Page<PersonaDetailsEntity> getAllByOrganizationId(Long id, Pageable pageable);

    void deleteByOrganizationId(Long organizationId);

    Page<PersonaDetailsEntity> findByOrganizationIdAndVersionId(
            Long organizationId, Integer versionId, Pageable pageable);

    Page<PersonaDetailsEntity> findByOrganizationIdOrderByVersionIdDesc(
            Long organizationId, Pageable pageable);
}
