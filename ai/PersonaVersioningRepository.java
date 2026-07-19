package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaVersioning;
import com.sharkdom.model.ai.PersonaMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaVersioningRepository extends JpaRepository<PersonaVersioning, Long> {

    Optional<PersonaVersioning> findTopByOrgIdAndPersonaModeOrderByVersionDesc(
            Long orgId,
            PersonaMode personaMode
    );

    Optional<PersonaVersioning> findByOrgIdAndPersonaModeAndVersion(
            Long orgId,
            PersonaMode personaMode,
            Integer version
    );

    List<PersonaVersioning> findByOrgIdOrderByVersionDesc(Long orgId);

    List<PersonaVersioning> findByOrgIdAndPersonaModeOrderByVersionDesc(
            Long orgId,
            PersonaMode personaMode
    );

}