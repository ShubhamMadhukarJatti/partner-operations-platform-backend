package com.sharkdom.partnertraining.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
/** @author Ayush Shrivastava */
public class CourseProgressService {

    @Autowired private UserCourseStageProgressRepository progressRepository;
    @Autowired private CourseStageRepository courseStageRepository;
    @Autowired private MyPartnerUserCourseStatusRepository repository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseCertificateRepository courseCertificateRepository;
    @Autowired private EmailService emailService;

    @Value("${env}") private String env;

    // ============================
    // MARK STAGE COMPLETE (USER)
    // ============================
    public void markStageCompleted(String userId, Long courseId, Long stageId) {
        log.info("Mark stage complete | userId={}, courseId={}, stageId={}", userId, courseId, stageId);
        UserCourseStageProgress p = progressRepository.findByUserIdAndCourseIdAndStageId(userId, courseId, stageId)
                .orElseGet(() -> { UserCourseStageProgress x = new UserCourseStageProgress(); x.setUserId(userId); x.setCourseId(courseId); x.setStageId(stageId); return x; });
        p.setCompleted(true); p.setCompletedAt(LocalDateTime.now()); progressRepository.save(p);
    }

    // ============================
    // MARK STAGE COMPLETE (ORG)
    // ============================
    public void markStageCompletedForMyCourses(Long courseId, Long stageId) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Mark stage complete | orgId={}, courseId={}, stageId={}", orgId, courseId, stageId);
        UserCourseStageProgress p = progressRepository.findByOrgIdAndCourseIdAndStageId(orgId, courseId, stageId)
                .orElseGet(() -> { UserCourseStageProgress x = new UserCourseStageProgress(); x.setOrgId(orgId); x.setCourseId(courseId); x.setStageId(stageId); return x; });
        p.setCompleted(true); p.setCompletedAt(LocalDateTime.now()); progressRepository.save(p);
    }

    // ============================
    // READINESS SCORE
    // ============================
    public int getReadinessScoreMyCourses(Long courseId) {
        Long orgId = Util.getOrgIdFromToken();
        long completed = progressRepository.countByOrgIdAndCourseIdAndCompletedTrue(orgId, courseId);
        long total = courseStageRepository.countByCourseIdAndActiveTrue(courseId);
        return total == 0 ? 0 : (int)((completed * 100) / total);
    }

    public int getReadinessScore(String userId, Long courseId) {
        long completed = progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
        long total = courseStageRepository.countByCourseIdAndActiveTrue(courseId);
        return total == 0 ? 0 : (int)((completed * 100) / total);
    }

    // ============================
    // STAGE PROGRESS
    // ============================
    public List<UserCourseStageProgress> getStageProgressMyCourses(Long courseId) {
        Long orgId = Util.getOrgIdFromToken();
        if (courseStageRepository.countByCourseIdAndActiveTrue(courseId) == 0) return Collections.emptyList();
        return progressRepository.findByOrgIdAndCourseId(orgId, courseId);
    }

    public List<UserCourseStageProgress> getStageProgress(String userId, Long courseId) {
        if (courseStageRepository.countByCourseIdAndActiveTrue(courseId) == 0) return Collections.emptyList();
        return progressRepository.findByUserIdAndCourseId(userId, courseId);
    }

    // ============================
    // ASSIGN COURSE
    // ============================
    public void assignCourse(AssignCourseRequest req) {
        Long assigningOrgId = Util.getOrgIdFromToken();
        MyPartnerUserCourseStatusEntity e = repository.findByCourseIdAndAssignedOrgId(req.getCourseId(), req.getAssignedOrgId())
                .orElseGet(MyPartnerUserCourseStatusEntity::new);
        e.setCourseId(req.getCourseId()); e.setAssignedOrgId(req.getAssignedOrgId()); e.setAssigningOrgId(assigningOrgId);
        e.setStatus(UserCourseStatus.ASSIGNED); repository.save(e);
        log.info("Course assigned | courseId={}", req.getCourseId());
    }

    // ============================
    // UPDATE STATUS + CERTIFICATE
    // ============================
    public void updateAssignedCourseStatus(UpdateAssignedCourseStatusRequest req) {
        MyPartnerUserCourseStatusEntity e = repository.findByCourseIdAndAssignedOrgId(req.getCourseId(), req.getAssignedOrgId())
                .orElseThrow(() -> new RuntimeException("Assigned course not found"));
        UserCourseStatus oldStatus = e.getStatus(), newStatus = req.getStatus();
        e.setStatus(newStatus); repository.save(e);

        if (UserCourseStatus.COMPLETED.equals(newStatus) && !UserCourseStatus.COMPLETED.equals(oldStatus)) {
            Course course = courseRepository.findById(req.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));
            if (course.getCertificateUrl() != null && !course.getCertificateUrl().isBlank()) {
                courseCertificateRepository.save(CourseCertificate.builder().courseId(req.getCourseId()).orgId(req.getAssignedOrgId()).certificateUrl(course.getCertificateUrl()).build());
                String utm = encrypt(req.getAssignedOrgId() + ":" + req.getCourseId());
                String baseUrl = env.equalsIgnoreCase("dev") ? "https://dev.sharkdom.com/partner-course/utm=" : "https://sharkdom.com/partner-course/utm=";
                emailService.sendByEmailAddTeamMember("download_certificate", Util.getUserFromToken(), baseUrl + utm, "ayshriv");
            }
        }
    }

    // ============================
    // DASHBOARD
    // ============================
    public CourseDashboardResponse getPartnerDashboardCourses(Long orgId, UserCourseStatus status, int page, int size) {
        List<CourseCardDto> continueCourses = repository.findAllByAssignedOrgIdAndStatus(orgId, UserCourseStatus.IN_PROGRESS)
                .stream().map(e -> mapToCard(e, courseRepository.findById(e.getCourseId()).orElseThrow())).toList();

        Pageable pageable = PageRequest.of(page, size);
        Page<MyPartnerUserCourseStatusEntity> pageResult =
                status == null ? repository.findByAssignedOrgId(orgId, pageable) :
                        status == UserCourseStatus.NOT_STARTED ? repository.findByAssignedOrgIdAndStatus(orgId, UserCourseStatus.ASSIGNED, pageable) :
                                repository.findByAssignedOrgIdAndStatus(orgId, status, pageable);

        List<CourseCardDto> courses = pageResult.getContent().stream()
                .map(e -> mapToCard(e, courseRepository.findById(e.getCourseId()).orElseThrow())).toList();

        return CourseDashboardResponse.builder().continueCourses(continueCourses).courses(courses)
                .totalElements(pageResult.getTotalElements()).totalPages(pageResult.getTotalPages()).currentPage(page).build();
    }

    private CourseCardDto mapToCard(MyPartnerUserCourseStatusEntity e, Course c) {
        return CourseCardDto.builder().courseId(c.getId()).title(c.getTitle()).thumbnailUrl(c.getCoverImageUrl())
                .modules(courseStageRepository.countByCourseIdAndActiveTrue(c.getId()))
                .durationInMinutes(c.getDurationMinutes()).level(c.getLevel().name())
                .status(e.getStatus()).progressPercentage(e.getStatus() == UserCourseStatus.COMPLETED ? 100 : 0).build();
    }

    // ============================
    // CERTIFICATES
    // ============================
    public List<CourseCertificateResponse> getCertificatesByUserId(String userId) {
        return courseCertificateRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    public List<CourseCertificateResponse> getCertificatesByOrgId() {
        return courseCertificateRepository.findByOrgId(Util.getOrgIdFromToken()).stream().map(this::mapToResponse).toList();
    }

    private CourseCertificateResponse mapToResponse(CourseCertificate c) {
        return CourseCertificateResponse.builder().id(c.getId()).courseId(c.getCourseId()).userId(c.getUserId())
                .orgId(c.getOrgId()).certificateUrl(c.getCertificateUrl()).build();
    }

    // ============================
    // ENCRYPTION (AES)
    // ============================
    private static final String ALGO = "AES";

    public String encrypt(String data) {
        try {
            String key = env.equalsIgnoreCase("dev") ? "Uz1FyvLoNnIKdGjMIRPDKccr" : "KzaFdvfoDOIFd9SMIQPDKcE1";
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), ALGO));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new ServiceException(ErrorMessages.SH116, e.getMessage()); }
    }
}