package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.FormDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormDetailsRepository extends JpaRepository<FormDetails,Long> {


    List<FormDetails> findByFormId(Long formId);

    List<FormDetails> findAllByOrganizationId(Long orgId);


    List<FormDetails> findByForm(String formId);

    Optional<FormDetails> findByFormAndOrganizationId(String form, Long orgId);

    Optional<FormDetails> findByFormIdAndOrganizationId(Long formId, Long orgId);
}
