package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.PpiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PpiRepository extends JpaRepository<PpiEntity,Long> {
    PpiEntity findByScriptId(String scriptId);

    Optional<PpiEntity> findTopByOrganization_IdOrderByCreationTimestampDesc(Long organizationId);

    List<PpiEntity> findByOrganization_Id(Long orgId);

    PpiEntity findByFormIdAndSheetId(String formId, String sheetId);

    Optional<PpiEntity>  findOneByOrganization_Id(Long orgId);
}
