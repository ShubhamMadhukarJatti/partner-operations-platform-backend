package com.sharkdom.service.template;

import com.sharkdom.entity.template.Template;
import com.sharkdom.entity.template.UserTemplates;
import com.sharkdom.model.template.UserTemplatesModel;
import com.sharkdom.repository.template.TemplateRepository;
import com.sharkdom.repository.template.UserTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TemplateRepository templateRepository;
    private final UserTemplateRepository userTemplateRepository;

    public TemplateService(UserTemplateRepository userTemplateRepository) {
        this.userTemplateRepository = userTemplateRepository;
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(long templateId) {
        return templateRepository.findById(templateId);
    }

    public Template createTemplate(Template template) {

        System.out.println("TemplateFound : " + template.toString());
        return templateRepository.save(template);
    }

    public Template updateTemplate(long templateId, Template updatedTemplate) {
        // Assuming you have a method to update specific fields in your entity
        // You can implement this method in Template class or use BeanUtils from Spring
        // For simplicity, let's assume such a method exists
        Template existingTemplate = new Template();
        try {
            existingTemplate = templateRepository.getByTemplateId(templateId);
            existingTemplate.updateFields(updatedTemplate);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return templateRepository.save(existingTemplate);
    }


    public void deleteTemplate(long templateId) {
        templateRepository.deleteById(templateId);
    }

    public List<Template> getAllTemplatesAssocitedWithUserId(String userId) {
        List<Long> list = userTemplateRepository.findTemplateByUserId(userId);
        return templateRepository.findAllByTemplateIdIn(list);
    }

    public void mapTemplateToUser(UserTemplatesModel templates) {
        if (templates.getTemplateIds() != null && !templates.getTemplateIds().isEmpty() && templates.getUserId() != null) {
            List<UserTemplates> userTemplatesList = new ArrayList<>();
            templates.getTemplateIds().forEach(templateId -> {
                UserTemplates userTemplates = UserTemplates.builder().userId(templates.getUserId()).templateId(templateId).build();
                userTemplatesList.add(userTemplates);
            });
            userTemplateRepository.saveAll(userTemplatesList);
        }

    }

    public void deleteTemplateOfUser(UserTemplatesModel templates) {
        if (templates.getTemplateIds() != null && !templates.getTemplateIds().isEmpty() && templates.getUserId() != null) {
            List<UserTemplates> userTemplatesList = new ArrayList<>();
            templates.getTemplateIds().forEach(templateId -> {
                UserTemplates userTemplates = UserTemplates.builder().userId(templates.getUserId()).templateId(templateId).build();
                userTemplatesList.add(userTemplates);
            });
            userTemplateRepository.deleteAll(userTemplatesList);
        }

    }
}
