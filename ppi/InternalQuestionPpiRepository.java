package com.sharkdom.repository.ppi;

import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.entity.ppi.InternalQuestion_Ppi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalQuestionPpiRepository extends JpaRepository<InternalQuestion_Ppi,Long> {
    List<InternalQuestion_Ppi> findByOrganization_Id(Long orgId);

    InternalQuestion_Ppi findByFormDetails_FormIdAndQuestionText(Long formId, String questionText);

    List<InternalQuestion_Ppi> findByOrganization_IdAndStatus(Long orgId, Question_Status questionStatus);


    List<?> findByFormDetails_FormIdAndStatus(Long aLong, Question_Status questionStatus);



}
