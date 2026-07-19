package com.sharkdom.repository.ppi;

import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.entity.ppi.WebHookQuestion_Ppi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookQuestionPpiRepository  extends JpaRepository<WebHookQuestion_Ppi,Long> {
    List<WebHookQuestion_Ppi> findByOrganization_id(Long orgId);


    List<?> findByFormDetails_FormAndStatus(String formId, Question_Status questionStatus);
}
