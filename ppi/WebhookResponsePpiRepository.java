package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.InternalQuestion_Ppi;
import com.sharkdom.entity.ppi.WebHookResponse_Ppi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookResponsePpiRepository extends JpaRepository<WebHookResponse_Ppi,Long> {
    List<WebHookResponse_Ppi> findByOrganization_id(Long orgId);

    List<WebHookResponse_Ppi> findByFormId(String formId);
}
