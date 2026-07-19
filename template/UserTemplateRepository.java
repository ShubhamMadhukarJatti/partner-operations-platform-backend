package com.sharkdom.repository.template;

import com.sharkdom.entity.template.UserTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTemplateRepository extends JpaRepository<UserTemplates, Long> {
    @Query("Select templateId from UserTemplates where userId=:userId")
    List<Long> findTemplateByUserId(String userId);

}
