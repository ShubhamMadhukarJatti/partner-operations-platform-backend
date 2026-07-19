package com.sharkdom.partnertraining.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final CourseRepository courseRepository;

    // ========================= CREATE LABEL =========================
    public Label createLabel(CreateLabelRequest req){
        var orgIdFromToken = Util.getOrgIdFromToken();
        log.debug("Check label exists | name={}",req.getName());
        labelRepository.findByNameIgnoreCase(req.getName()).ifPresent(l->{ throw new ServiceException(ErrorMessages.SH161,req.getName()); });
        Label label=new Label(); label.setName(req.getName().trim());
        label.setOrganizationId(orgIdFromToken);
        Label saved=labelRepository.save(label);
        log.info("Label created | id={}",saved.getId());
        return saved;
    }

    // ========================= ASSIGN LABELS =========================
    public Course assignLabelsToCourse(Long courseId,AssignLabelsRequest req){
        log.debug("Fetch course | courseId={}",courseId);
        Course course=courseRepository.findById(courseId).orElseThrow(()->new ServiceException(ErrorMessages.SH163,courseId));

        Set<Label> labels=req.getLabelIds().stream()
                .map(id->labelRepository.findById(id).orElseThrow(()->new ServiceException(ErrorMessages.SH162,id)))
                .collect(Collectors.toSet());

        course.getLabels().clear(); course.getLabels().addAll(labels);
        Course updated=courseRepository.save(course);

        log.info("Labels assigned | courseId={}, count={}",courseId,labels.size());
        return updated;
    }

    // ========================= GET ALL LABELS =========================
    public List<LabelResponse> getAllLabels(){
        log.info("Fetch all labels");
        return labelRepository.findAll().stream()
                .sorted((a,b)->a.getName().compareToIgnoreCase(b.getName()))
                .map(l->LabelResponse.builder().id(l.getId()).name(l.getName()).build())
                .toList();
    }

    public List<LabelResponse> getAllLabelsByOrganizationId(Long organizationId){
        log.info("Fetch all labels");
        return labelRepository.findByOrganizationId(organizationId).stream()
                .sorted((a,b)->a.getName().compareToIgnoreCase(b.getName()))
                .map(l->LabelResponse.builder().id(l.getId()).name(l.getName()).build())
                .toList();
    }
}