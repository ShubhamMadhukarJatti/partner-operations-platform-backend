package com.sharkdom.repository.template;

import com.sharkdom.entity.template.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Template getByTemplateId(long templateId);
    List<Template> findAllByTemplateIdIn(List<Long> templateIds);

    // You can add custom queries if needed

}
