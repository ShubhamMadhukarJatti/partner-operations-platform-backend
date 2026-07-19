package com.sharkdom.partnertraining.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.enums.StageType;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import com.sharkdom.util.aws.service.AmazonS3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    // ============================
    // DEPENDENCIES
    // ============================
    private final AmazonS3Service amazonS3Service;
    private final CourseRepository courseRepository;
    private final LabelRepository labelRepository;
    private final OrganizationRepository organizationRepository;
    private final CourseStageRepository courseStageRepository;
    private final StageContentRepository contentRepository;
    private final StageQuizRepository stageQuizRepository;
    private final CourseAssignmentRuleRepository assignmentRuleRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${logo.s3Path}")
    private String logoS3Path;

    // ============================
    // COURSE CRUD OPERATIONS
    // ============================
    public CourseResponse createCourse(CreateCourseRequest request) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Creating course with title={}", request.getTitle());
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH101, orgId));
        Course course = new Course();
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription());
        course.setCoverImageUrl(request.getCoverImageUrl());
        course.setLevel(request.getLevel());
        course.setDurationMinutes(request.getDurationMinutes());
        course.setPublished(false);
        course.setCreatedBy(organization);
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            Set<Label> labels = request.getLabelIds().stream().map(this::getLabelOrThrow).collect(Collectors.toSet());
            course.setLabels(labels);
        }
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with id={}", savedCourse.getId());
        return mapToResponse(savedCourse);
    }

    public CourseResponse getCourseById(Long courseId) {
        log.info("Fetching course details id={}", courseId);
        Course course = getCourseOrThrow(courseId);
        return mapToResponse(course);
    }

    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) {
        log.info("Updating course id={}", courseId);
        Course course = getCourseOrThrow(courseId);
        if (request.getTitle() != null) course.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCoverImageUrl() != null) course.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getDurationMinutes() != null) course.setDurationMinutes(request.getDurationMinutes());
        if (request.getLabelIds() != null) {
            Set<Label> labels = request.getLabelIds().stream().map(this::getLabelOrThrow).collect(Collectors.toSet());
            course.getLabels().clear();
            course.getLabels().addAll(labels);
        }
        Course updated = courseRepository.save(course);
        log.info("Course updated successfully id={}", updated.getId());
        return mapToResponse(updated);
    }

    public void publishCourse(Long courseId) {
        log.info("Attempting to publish course id={}", courseId);
        Course course = getCourseOrThrow(courseId);
        if (Boolean.TRUE.equals(course.getPublished())) {
            log.warn("Course already published id={}", courseId);
            return;
        }
        course.setPublished(true);
        courseRepository.save(course);
        log.info("Course published successfully id={}", courseId);
    }

    // ============================
    // PRIVATE HELPERS
    // ============================
    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH163, courseId));
    }

    private Label getLabelOrThrow(Long labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH162, labelId));
    }

    // ============================
    // FILE UPLOAD (S3)
    // ============================
    @Transactional
    public CoverImageUploadResponseDto uploadFile(MultipartFile file) {
        try {
            long organizationId = 2345;
            var s3Client = amazonS3Service.getS3Instance();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            String fileName = logoS3Path + organizationId + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            s3Client.putObject(new PutObjectRequest("sharkdom.co.in", fileName, file.getInputStream(), metadata));
            String fileUrl = "https://s3.ap-south-1.amazonaws.com/sharkdom.co.in/" + fileName;
            return new CoverImageUploadResponseDto(fileUrl);
        } catch (Exception e) {
            log.error("Exception occurred while uploading file", e);
            throw new RuntimeException("Unable to upload file");
        }
    }

    // ============================
    // STAGE MANAGEMENT
    // ============================
    public CourseStage createStage(Long courseId, CreateStageRequest request) {
        log.info("Creating stage for courseId={}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        int nextOrder = courseStageRepository.countByCourseIdAndActiveTrue(courseId) + 1;
        CourseStage stage = new CourseStage();
        stage.setCourse(course);
        stage.setTitle(request.getTitle());
        stage.setType(request.getType());
        stage.setStageOrder(nextOrder);
        CourseStage saved = courseStageRepository.save(stage);
        log.info("Stage created with id={} for courseId={}", saved.getId(), courseId);
        return saved;
    }

    @Transactional
    public void reorderStages(Long courseId, ReorderStagesRequest request) {
        log.info("Reordering stages for courseId={}", courseId);
        List<CourseStage> stages = courseStageRepository.findByCourseIdAndActiveTrue(courseId);
        if (stages.size() != request.getStageIds().size()) throw new IllegalStateException("Stage count mismatch");
        int order = 1;
        for (Long stageId : request.getStageIds()) {
            CourseStage stage = stages.stream().filter(s -> s.getId().equals(stageId)).findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Invalid stage id: " + stageId));
            stage.setStageOrder(order++);
        }
        courseStageRepository.saveAll(stages);
        log.info("Stages reordered successfully for courseId={}", courseId);
    }

    @Transactional
    public void deleteStageP(Long stageId) {
        log.info("Deleting stage with id={}", stageId);
        CourseStage stage = courseStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage not found"));
        Long courseId = stage.getCourse().getId();
        Integer deletedOrder = stage.getStageOrder();
        courseStageRepository.delete(stage);
        List<CourseStage> remainingStages = courseStageRepository.findByCourseIdAndActiveTrueOrderByStageOrderAsc(courseId);
        for (CourseStage s : remainingStages) if (s.getStageOrder() > deletedOrder) s.setStageOrder(s.getStageOrder() - 1);
        courseStageRepository.saveAll(remainingStages);
        log.info("Stage deleted and order re-adjusted for courseId={}", courseId);
    }

    @Transactional
    public void deleteStage(Long stageId) {
        log.info("Deleting stage with id={}", stageId);
        CourseStage stage = courseStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage not found"));
        stage.setActive(false);
        courseStageRepository.save(stage);
    }

    // ============================
    // STAGE CONTENT MANAGEMENT
    // ============================
    public StageContent saveContent(Long stageId, SaveStageContentRequest request) {
        log.info("Saving content for stageId={}", stageId);
        CourseStage stage = courseStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage not found"));
        StageContent content = contentRepository.findByStageId(stageId)
                .orElseGet(() -> { StageContent sc = new StageContent(); sc.setStage(stage); return sc; });
        if (request.getContent() != null) content.setContent(request.getContent());
        if (request.getChapterTitle() != null) content.setChapterTitle(request.getChapterTitle());
        if (request.getContentType() != null) content.setContentType(request.getContentType());
        if (request.getThumbnailUrl() != null) content.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDriveLink() != null) content.setDriveLink(request.getDriveLink());
        if (request.getDocumentLink() != null) content.setDocumentLink(request.getDocumentLink());
        if (request.getImageUrls() != null) {
            content.getImages().clear();
            int order = 1;
            for (String url : request.getImageUrls()) {
                StageContentImage image = new StageContentImage();
                image.setStageContent(content);
                image.setImageUrl(url);
                image.setImageOrder(order++);
                content.getImages().add(image);
            }
        }
        StageContent saved = contentRepository.save(content);
        log.info("Saved stage content for stageId={} with {} images", stageId, saved.getImages().size());
        return saved;
    }

    public StageContentResponse getStageContent(Long stageId) {
        log.info("Fetching content for stageId={}", stageId);
        StageContent content = contentRepository.findByStageId(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage content not found"));
        return StageContentResponse.builder()
                .stageId(stageId)
                .content(content.getContent())
                .contentType(content.getContentType())
                .thumbnailUrl(content.getThumbnailUrl())
                .driveLink(content.getDriveLink())
                .documentLink(content.getDocumentLink())
                .chapterTitle(content.getChapterTitle())
                .images(content.getImages().stream()
                        .sorted((a, b) -> a.getImageOrder() - b.getImageOrder())
                        .map(img -> StageContentResponse.ImageResponse.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .order(img.getImageOrder())
                                .build())
                        .toList())
                .build();
    }

    // ============================
    // QUIZ MANAGEMENT
    // ============================
    public StageQuiz saveQuiz(Long stageId, SaveQuizRequest request) {
        log.info("Saving quiz for stageId={}", stageId);
        CourseStage stage = courseStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage not found"));
        StageQuiz quiz = stageQuizRepository.findByStageId(stageId)
                .orElseGet(() -> { StageQuiz q = new StageQuiz(); q.setStage(stage); return q; });
        quiz.setTitle(request.getTitle());
        quiz.getQuestions().clear();
        int order = 1;
        for (SaveQuizRequest.QuestionRequest q : request.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestion(q.getQuestion());
            question.setCorrectAnswer(q.getCorrectAnswer());
            question.setQuestionOrder(order++);
            try { question.setOptions(objectMapper.writeValueAsString(q.getOptions())); }
            catch (Exception e) { throw new IllegalStateException("Invalid options format"); }
            quiz.getQuestions().add(question);
        }
        StageQuiz saved = stageQuizRepository.save(quiz);
        log.info("Quiz saved with {} questions", saved.getQuestions().size());
        return saved;
    }

    public QuizResponse getQuizByStageId(Long stageId) {
        log.info("Fetching quiz for stageId={}", stageId);
        StageQuiz quiz = stageQuizRepository.findByStageId(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
        return QuizResponse.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .questions(quiz.getQuestions().stream()
                        .sorted((a, b) -> a.getQuestionOrder() - b.getQuestionOrder())
                        .map(q -> {
                            try {
                                return QuizResponse.QuestionResponse.builder()
                                        .id(q.getId())
                                        .question(q.getQuestion())
                                        .options(objectMapper.readValue(q.getOptions(), List.class))
                                        .order(q.getQuestionOrder())
                                        .build();
                            } catch (Exception e) {
                                throw new IllegalStateException("Failed to parse options");
                            }
                        }).toList())
                .build();
    }

    public boolean verifyAnswer(Long stageId, String userAnswer) {
        StageQuiz quiz = stageQuizRepository.findByStageId(stageId)
                .orElseThrow(() -> new RuntimeException("Quiz not found for stageId: " + stageId));
        QuizQuestion question = quizQuestionRepository.findByQuiz(quiz)
                .orElseThrow(() -> new RuntimeException("Question not found for quizId: " + quiz.getId()));
        return question.getCorrectAnswer() != null && question.getCorrectAnswer().equalsIgnoreCase(userAnswer);
    }

    // ============================
    // COURSE FETCH / DETAILS
    // ============================
    public List<CourseStageResponse> getStagesByCourseId(Long courseId) {
        log.info("Fetching stages for courseId={}", courseId);
        if (!courseRepository.existsById(courseId)) throw new EntityNotFoundException("Course not found");
        return courseStageRepository.findByCourseIdAndActiveTrueOrderByStageOrderAsc(courseId)
                .stream().map(stage -> {
                    boolean contentCreated = false;
                    if (stage.getType() == StageType.CONTENT) contentCreated = contentRepository.existsByStageId(stage.getId());
                    if (stage.getType() == StageType.QUIZ) contentCreated = stageQuizRepository.existsByStageId(stage.getId());
                    return CourseStageResponse.builder()
                            .stageId(stage.getId())
                            .title(stage.getTitle())
                            .type(stage.getType())
                            .order(stage.getStageOrder())
                            .isContentCreated(contentCreated)
                            .build();
                }).toList();
    }

    public StageResponse getStageById(Long stageId) {
        log.info("Fetching stage details for stageId={}", stageId);
        CourseStage stage = courseStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Stage not found"));
        return StageResponse.builder()
                .stageId(stage.getId())
                .title(stage.getTitle())
                .type(stage.getType())
                .order(stage.getStageOrder())
                .courseId(stage.getCourse().getId())
                .build();
    }

    public CourseDetailsResponse getCourseDetails(Long courseId) {
        log.info("Fetching full course details for courseId={}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        List<CourseStage> stages = courseStageRepository.findByCourseIdAndActiveTrueOrderByStageOrderAsc(courseId);
        List<StageDetailsResponse> stageResponses = stages.stream().map(stage -> {
            StageDetailsResponse.StageDetailsResponseBuilder builder =
                    StageDetailsResponse.builder()
                            .stageId(stage.getId())
                            .title(stage.getTitle())
                            .type(stage.getType())
                            .order(stage.getStageOrder());
            if (stage.getType() == StageType.CONTENT) try { builder.content(getStageContent(stage.getId())); } catch (Exception ignored) {}
            if (stage.getType() == StageType.QUIZ) try { builder.quiz(getQuizByStageId(stage.getId())); } catch (Exception ignored) {}
            return builder.build();
        }).toList();
        return CourseDetailsResponse.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .coverImageUrl(course.getCoverImageUrl())
                .level(course.getLevel())
                .labelNames(course.getLabels().stream().map(Label::getName).collect(Collectors.toSet()))
                .stageCount(stageResponses.size())
                .durationMinutes(course.getDurationMinutes())
                .published(course.getPublished())
                .stages(stageResponses)
                .build();
    }

    // ============================
    // COURSE LISTING
    // ============================
    public List<CourseCardResponse> getAllCourses() {
        var orgId = Util.getOrgIdFromToken();
        log.info("Fetching course list for card view");
        return courseRepository.findAllByCreatedBy_IdAndPublishedTrue(orgId)
                .stream().map(course -> CourseCardResponse.builder()
                        .courseId(course.getId())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .coverImageUrl(course.getCoverImageUrl())
                        .level(course.getLevel())
                        .durationMinutes(course.getDurationMinutes())
                        .published(course.getPublished())
                        .stageCount(courseStageRepository.countByCourseIdAndActiveTrue(course.getId()))
                        .completionPercentage(null)
                        .build()).toList();
    }

    public List<Course> getUnpublishedCoursesByOrganization(Long organizationId) {
        return courseRepository.findAllByCreatedBy_IdAndPublishedFalse(organizationId);
    }

    // ============================
    // ASSIGNMENT RULES
    // ============================
    @Transactional
    public void saveAssignmentRules(Long courseId, CourseAssignmentRuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        CourseAssignmentRule rule = assignmentRuleRepository.findByCourseId(courseId)
                .orElseGet(() -> CourseAssignmentRule.builder().course(course).build());
        rule.setTier(String.join(",", request.getTiers()));
        rule.setGeography(String.join(",", request.getGeographies()));
        rule.setProgramType(String.join(",", request.getProgramTypes()));
        assignmentRuleRepository.save(rule);
    }

    public CourseAssignmentRuleResponse getAssignmentRules(Long courseId) {
        CourseAssignmentRule rule = assignmentRuleRepository.findByCourseId(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment rules not found"));
        return CourseAssignmentRuleResponse.builder()
                .courseId(courseId)
                .tiers(List.of(rule.getTier().split(",")))
                .geographies(List.of(rule.getGeography().split(",")))
                .programTypes(List.of(rule.getProgramType().split(",")))
                .build();
    }

    // ============================
    // CERTIFICATE MANAGEMENT
    // ============================
    @Transactional
    public void updateCourseCertificateUrl(Long courseId, String certificateUrl) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        course.setCertificateUrl(certificateUrl);
        courseRepository.save(course);
    }

    public SharkdomApiResponse<List<CourseCertificateResponseDTO>> getCoursesByCreatedOrg() {
        Long ordId = Util.getOrgIdFromToken();
        List<Course> courses = courseRepository.findAllByCreatedBy_IdAndPublishedTrue(ordId);
        if (courses.isEmpty()) return new SharkdomApiResponse<>(true, "No courses found for this organization", List.of());
        List<CourseCertificateResponseDTO> response = courses.stream()
                .map(course -> CourseCertificateResponseDTO.builder()
                        .courseId(course.getId())
                        .courseName(course.getTitle())
                        .certificateUrl(course.getCertificateUrl())
                        .build())
                .collect(Collectors.toList());
        return new SharkdomApiResponse<>(true, "Courses fetched successfully", response);
    }

    // ============================
    // PUBLISH STATUS
    // ============================
    public void updatePublishStatus(Long courseId, Boolean published) {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        optionalCourse.ifPresentOrElse(course -> {
            course.setPublished(published);
            courseRepository.save(course);
        }, () -> {
            throw new ResourceNotFoundException(ErrorMessages.valueOf("Course not found with id: " + courseId));
        });
    }

    // ============================
    // MAPPER
    // ============================
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .coverImageUrl(course.getCoverImageUrl())
                .level(course.getLevel())
                .durationMinutes(course.getDurationMinutes())
                .published(course.getPublished())
                .labels(course.getLabels().stream().map(Label::getName).collect(Collectors.toSet()))
                .build();
    }
}