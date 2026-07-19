package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.ScriptDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptDetailsRepository extends JpaRepository<ScriptDetails ,Long> {
    ScriptDetails findByFormIdAndSheetId(String formId, String sheetId);

    List<ScriptDetails> findByOrgId(Long orgIdFromToken);

    ScriptDetails findOneByOrgId(Long orgId);
}
