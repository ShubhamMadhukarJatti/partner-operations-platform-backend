package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.PilotProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PilotProgramRepository extends JpaRepository<PilotProgram, Long> {
    List<PilotProgram> findAllByOrganizationId(Long organizationId);
}
