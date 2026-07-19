package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<PersonaEntity, Long> {
    List<PersonaEntity> getAllByOrganizationId(Long id);
    Optional<PersonaEntity> findFirstByOrganizationId(Long id);
    List<PersonaEntity> findByOrganizationIdAndVersionId(Long organizationId, Integer versionId);
    @Query("SELECT MAX(p.versionId) FROM PersonaEntity p WHERE p.organizationId = :organizationId")
    Integer findTopVersionByOrganizationId(@Param("organizationId") Long organizationId);
}
