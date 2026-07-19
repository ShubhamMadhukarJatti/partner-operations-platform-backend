package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.InternalResponse_Ppi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalResponsePpiRepository extends JpaRepository<InternalResponse_Ppi,Long> {

    List<InternalResponse_Ppi> findByOrganization_Id(Long organizationId);
    List<InternalResponse_Ppi> findByOrganization_IdAndFormId(Long id, Long formId);

    List<InternalResponse_Ppi> findByFormId(Long formId);

    int countByFormId(Long formId);
//    InternalResponse_Ppi findByFormId(Long formId);
}
