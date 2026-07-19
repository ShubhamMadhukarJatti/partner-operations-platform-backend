package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.PartnerProgramStepper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerProgramStepperRepository
        extends JpaRepository<PartnerProgramStepper, Long> {

    Optional<PartnerProgramStepper> findByOrganizationId(Long organizationId);
}

