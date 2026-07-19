package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.model.PersonaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaStatusRepository extends JpaRepository<PersonaStatusEntity, Long> {

    @Query("SELECT p FROM PersonaStatusEntity p WHERE p.organizationId = :id ORDER BY p.creationTimestamp DESC LIMIT 1")
    PersonaStatusEntity getByOrganizationId(Long id);

    // Fetch all persona status entities where orgId is in given list and status is COMPLETED
    List<PersonaStatusEntity> findByOrganizationIdInAndPersonaStatus(List<Long> organizationIds, PersonaStatus personaStatus);

    Optional<PersonaStatusEntity> findFirstByOrganizationIdAndPersonaStatus(Long id, PersonaStatus personaStatus);

    Optional<PersonaStatusEntity> findTopByOrganizationIdOrderByVersionDesc(Long organizationId);

    PersonaStatusEntity findTopByOrganizationIdOrderByVersionIdDesc(Long orgId);

    PersonaStatusEntity findTopByOrganizationIdAndVersionIdOrderByIdDesc(Long organizationId,Integer versionId);

    PersonaStatusEntity findByOrganizationIdAndVersionId(Long organizationId, Integer versionId);


}
