package com.sharkdom.partnertraining.controller;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.service.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/partner/training")
public class PartnerTrainingRestController {

    private final DashBoardService dashBoardService;
    private final LabelService labelService;
    private final CourseService courseService;
    private final PartnerPortalCourseShareService partnerPortalCourseShareService;
    private final CourseProgressService courseProgressService;
    private final UserCourseStatusService userCourseStatusService;
    private final DriveToGcpService driveToGcpService;

    @Autowired
    public PartnerTrainingRestController(DashBoardService dashBoardService, LabelService labelService, CourseService courseService, PartnerPortalCourseShareService partnerPortalCourseShareService, CourseProgressService courseProgressService, UserCourseStatusService userCourseStatusService, DriveToGcpService driveToGcpService) {
        this.dashBoardService = dashBoardService;
        this.labelService = labelService;
        this.courseService = courseService;
        this.partnerPortalCourseShareService = partnerPortalCourseShareService;
        this.courseProgressService = courseProgressService;
        this.userCourseStatusService = userCourseStatusService;
        this.driveToGcpService = driveToGcpService;
    }

    @Operation(
            summary = "Create a new label",
            description = "Creates a new label that can be assigned to courses"
    )
    @PostMapping("/labels/add")
    public SharkdomApiResponse<Label> createLabel(
            @Valid @RequestBody CreateLabelRequest request) {
        log.info("Creating new label with name={}", request.getName());
        Label label = labelService.createLabel(request);
        log.info("Label created successfully with id={}", label.getId());
        return new SharkdomApiResponse<>(
                true,
                "Label created successfully",
                label
        );
    }

    @Operation(
            summary = "Assign labels to a course",
            description = "Replaces existing labels of a course with provided labels"
    )
    @PostMapping("course/{id}/labels")
    public SharkdomApiResponse<Course> assignLabels(
            @PathVariable Long id,
            @RequestBody AssignLabelsRequest request) {
        log.info("Assigning labels to courseId={}, labelIds={}",
                id, request.getLabelIds());
        Course course = labelService.assignLabelsToCourse(id, request);
        log.info("Labels assigned successfully to courseId={}", id);
        return new SharkdomApiResponse<>(
                true,
                "Labels assigned to course successfully",
                course
        );
    }

    @Operation(
            summary = "Get all labels",
            description = "Returns all available labels for dropdown selection"
    )
    @GetMapping("/labels")
    public SharkdomApiResponse<List<LabelResponse>> getAllLabels() {
        log.info("GET /api/labels called");
        List<LabelResponse> labels = labelService.getAllLabels();
        log.info("Fetched {} labels", labels.size());
        return new SharkdomApiResponse<>(
                true,
                "Labels fetched successfully",
                labels
        );
    }

    @Operation(
            summary = "Create a new course",
            description = "Creates a draft course with optional labels"
    )
    @PostMapping("/courses/add")
    public SharkdomApiResponse<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        log.info("POST /api/courses called");
        CourseResponse response =
                courseService.createCourse(request);
        return new SharkdomApiResponse<>(
                true,
                "Course created successfully",
                response
        );
    }

    @Operation(
            summary = "Get course by ID",
            description = "Fetch full course details for view or edit"
    )
    @GetMapping("/courses/{id}")
    public SharkdomApiResponse<CourseResponse> getCourse(
            @PathVariable Long id) {
        log.info("GET /api/courses/{} called", id);
        return new SharkdomApiResponse<>(
                true,
                "Course fetched successfully",
                courseService.getCourseById(id)
        );
    }

    @Operation(
            summary = "Update course",
            description = "Partially updates a course (wizard-friendly)"
    )
    @PatchMapping("/course/{id}/update")
    public SharkdomApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @RequestBody UpdateCourseRequest request) {
        log.info("PATCH /api/courses/{} called", id);
        return new SharkdomApiResponse<>(
                true,
                "Course updated successfully",
                courseService.updateCourse(id, request)
        );
    }

    @Operation(
            summary = "Publish course",
            description = "Marks a course as published after validation"
    )
    @PostMapping("/courses/{id}/publish")
    public SharkdomApiResponse<Void> publishCourse(
            @PathVariable Long id) {
        log.info("POST /api/courses/{}/publish called", id);
        courseService.publishCourse(id);
        return new SharkdomApiResponse<>(
                true,
                "Course published successfully",
                null
        );
    }

    @Operation(
            summary = "Upload file",
            description = "Uploads a file to S3 and returns the public file URL"
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public SharkdomApiResponse<CoverImageUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading file: name={}, size={}",
                file.getOriginalFilename(), file.getSize());
        CoverImageUploadResponseDto response =
                courseService.uploadFile(file);
        log.info("File uploaded successfully. URL={}", response.getFileUrl());
        return new SharkdomApiResponse<>(
                true,
                "File uploaded successfully",
                response
        );
    }

    @Operation(summary = "Create a new stage for course")
    @PostMapping("/courses/{courseId}/stages")
    public SharkdomApiResponse<CourseStage> createStage(
            @PathVariable Long courseId,
            @RequestBody CreateStageRequest request) {
        log.info("POST /api/courses/{}/stages", courseId);
        CourseStage stage = courseService.createStage(courseId, request);
        return new SharkdomApiResponse<>(
                true,
                "Stage created successfully",
                stage
        );
    }

    @Operation(summary = "Create or update stage content")
    @PostMapping("/courses/stages/{stageId}/content")
    public SharkdomApiResponse<StageContent> saveStageContent(
            @PathVariable Long stageId,
            @RequestBody SaveStageContentRequest request) {
        log.info("POST /api/stages/{}/content", stageId);
        StageContent content = courseService.saveContent(stageId, request);
        return new SharkdomApiResponse<>(
                true,
                "Stage content saved successfully",
                content
        );
    }

    @Operation(
            summary = "Reorder course stages",
            description = "Rearrange stages using drag-and-drop order"
    )
    @PutMapping("/courses/{courseId}/stages/reorder")
    public SharkdomApiResponse<Void> reorderStages(
            @PathVariable Long courseId,
            @RequestBody ReorderStagesRequest request) {
        log.info("PUT /api/courses/{}/stages/reorder", courseId);
        courseService.reorderStages(courseId, request);
        return new SharkdomApiResponse<>(
                true,
                "Stages reordered successfully",
                null
        );
    }

    @Operation(
            summary = "Get stage content",
            description = "Fetch text and images of a stage for preview or edit"
    )
    @GetMapping("/courses/stages/{stageId}/content")
    public SharkdomApiResponse<StageContentResponse> getStageContent(
            @PathVariable Long stageId) {
        log.info("GET /api/stages/{}/content", stageId);
        StageContentResponse response = courseService.getStageContent(stageId);
        return new SharkdomApiResponse<>(
                true,
                "Stage content fetched successfully",
                response
        );
    }

    @Operation(summary = "Create or update quiz for a stage")
    @PostMapping("courses/stages/{stageId}/quiz")
    public SharkdomApiResponse<Void> saveQuiz(
            @PathVariable Long stageId,
            @RequestBody SaveQuizRequest request) {
        log.info("POST /api/stages/{}/quiz", stageId);
        courseService.saveQuiz(stageId, request);
        return new SharkdomApiResponse<>(
                true,
                "Quiz saved successfully",
                null
        );
    }

    @Operation(summary = "Get quiz by stage id")
    @GetMapping("/courses/stages/{stageId}/quiz")
    public SharkdomApiResponse<QuizResponse> getQuiz(
            @PathVariable Long stageId) {
        log.info("GET /api/stages/{}/quiz", stageId);
        return new SharkdomApiResponse<>(
                true,
                "Quiz fetched successfully",
                courseService.getQuizByStageId(stageId)
        );
    }

    @Operation(
            summary = "Get stages by course ID",
            description = "Returns ordered list of stages for a course"
    )
    @GetMapping("/courses/{courseId}/stages")
    public SharkdomApiResponse<List<CourseStageResponse>> getStages(
            @PathVariable Long courseId) {
        log.info("GET /api/courses/{}/stages", courseId);
        return new SharkdomApiResponse<>(
                true,
                "Stages fetched successfully",
                courseService.getStagesByCourseId(courseId)
        );
    }

    @Operation(
            summary = "Get stage by ID",
            description = "Fetch stage basic details by stage ID"
    )
    @GetMapping("/courses/stages/{stageId}")
    public SharkdomApiResponse<StageResponse> getStageById(
            @PathVariable Long stageId) {
        log.info("GET /api/stages/{}", stageId);
        return new SharkdomApiResponse<>(
                true,
                "Stage fetched successfully",
                courseService.getStageById(stageId)
        );
    }

    @Operation(
            summary = "Get complete course details",
            description = "Returns course with stages, content, quizzes for preview or builder"
    )
    @GetMapping("/courses/{courseId}/details")
    public SharkdomApiResponse<CourseDetailsResponse> getCourseDetails(
            @PathVariable Long courseId) {
        log.info("GET /api/courses/{}/details", courseId);
        return new SharkdomApiResponse<>(
                true,
                "Course details fetched successfully",
                courseService.getCourseDetails(courseId)
        );
    }

    @Operation(
            summary = "Get all courses (card view)",
            description = "Returns list of courses optimized for listing UI"
    )
    @GetMapping("/courses")
    public SharkdomApiResponse<List<CourseCardResponse>> getCourses() {
        log.info("GET /api/courses");
        return new SharkdomApiResponse<>(
                true,
                "Courses fetched successfully",
                courseService.getAllCourses()
        );
    }

    @Operation(
            summary = "Delete stage",
            description = "Deletes a stage along with its content, images and quiz"
    )
    @DeleteMapping("/course/stages/{stageId}")
    public SharkdomApiResponse<Void> deleteStage(
            @PathVariable Long stageId) {
        log.info("DELETE /api/stages/{}", stageId);
        courseService.deleteStage(stageId);
        return new SharkdomApiResponse<>(
                true,
                "Stage deleted successfully",
                null
        );
    }

    @Operation(
            summary = "Save course assignment rules",
            description = "Creates or updates assignment rules for a course"
    )
    @PostMapping("/courses/{courseId}/assignment/rules")
    public SharkdomApiResponse<Void> saveAssignmentRules(
            @PathVariable Long courseId,
            @RequestBody CourseAssignmentRuleRequest request
    ) {
        log.info("Saving assignment rules for courseId={}", courseId);

        courseService.saveAssignmentRules(courseId, request);

        log.info("Assignment rules saved successfully for courseId={}", courseId);
        return new SharkdomApiResponse<>(
                true,
                "Assignment rules saved successfully",
                null
        );
    }

    @Operation(
            summary = "Get course assignment rules",
            description = "Fetch assignment rules configured for a course"
    )
    @GetMapping("/courses/{courseId}/assignment/rules")
    public SharkdomApiResponse<CourseAssignmentRuleResponse> getAssignmentRules(
            @PathVariable Long courseId
    ) {
        log.info("Fetching assignment rules for courseId={}", courseId);

        CourseAssignmentRuleResponse response =
                courseService.getAssignmentRules(courseId);

        log.info("Assignment rules fetched successfully for courseId={}", courseId);
        return new SharkdomApiResponse<>(
                true,
                "Assignment rules fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Share Course Invite Link",
            description = "Generate and share course invite link with a single user"
    )
    @PostMapping("/courses/invite")
    public SharkdomApiResponse<CourseShareResponse> shareCourse(
            @RequestBody CourseShareRequest request
    ) {
        log.info(
                "Received course share request | courseId={}, receiverEmail={}",
                request.getCourseId(),
                request.getReceiverUserEmail()
        );
        CourseShareResponse response = partnerPortalCourseShareService.shareCourse(request);
        log.info(
                "Course share API completed | shareId={}, courseId={}",
                response.getId(),
                response.getCourseId()
        );
        return new SharkdomApiResponse<>(
                true,
                "Course shared successfully.",
                response
        );
    }

    @Operation(
            summary = "Mark stage as completed",
            description = "Marks a course stage as completed for logged-in user"
    )
    @PostMapping("/courses/{courseId}/stages/{stageId}/complete/users/{userId}")
    public SharkdomApiResponse<Void> completeStage(
            @PathVariable Long courseId,
            @PathVariable Long stageId,
            @PathVariable String userId
    ) {
        log.info(
                "Stage completion request | userId={}, courseId={}, stageId={}",
                userId, courseId, stageId
        );
        courseProgressService.markStageCompleted(
                userId, courseId, stageId
        );

        return new SharkdomApiResponse<>(
                true,
                "Stage marked as completed",
                null
        );
    }

    @Operation(
            summary = "Mark stage as completed for my courses",
            description = "Marks a course stage as completed for logged-in org"
    )
    @PostMapping("/my/partner/courses/{courseId}/stages/{stageId}/complete")
    public SharkdomApiResponse<Void> completeStageMyCourses(
            @PathVariable Long courseId,
            @PathVariable Long stageId
    ) {
        log.info(
                "Stage completion request | courseId={}, stageId={}",
                courseId, stageId
        );
        courseProgressService.markStageCompletedForMyCourses(courseId,stageId);

        return new SharkdomApiResponse<>(
                true,
                "Stage marked as completed",
                null
        );
    }

    @Operation(
            summary = "Get course readiness score",
            description = "Returns readiness percentage based on completed stages"
    )
    @GetMapping("/courses/{courseId}/readiness/users/{userId}")
    public SharkdomApiResponse<Map<String, Object>> getReadinessScore(
            @PathVariable Long courseId,
            @PathVariable String userId
    ) {

        log.info(
                "Fetching readiness score | userId={}, courseId={}",
                userId, courseId
        );

        int readiness =
                courseProgressService.getReadinessScore(
                        userId, courseId
                );

        return new SharkdomApiResponse<>(
                true,
                "Readiness score calculated successfully",
                Map.of(
                        "courseId", courseId,
                        "readinessScore", readiness
                )
        );
    }

    @GetMapping("/my/partner/courses/{courseId}/readiness")
    public SharkdomApiResponse<Map<String, Object>> getReadinessScoreForMyCourses(
            @PathVariable Long courseId
    ) {
        log.info(
                "Fetching readiness score |courseId={}",
                courseId
        );
        int readiness =
                courseProgressService.getReadinessScoreMyCourses(courseId);
        return new SharkdomApiResponse<>(
                true,
                "Readiness score calculated successfully",
                Map.of(
                        "courseId", courseId,
                        "readinessScore", readiness
                )
        );
    }

    @Operation(
            summary = "Get course progress",
            description = "Returns stage-wise progress for logged-in user"
    )
    @GetMapping("/courses/{courseId}/progress/users/{userId}")
    public SharkdomApiResponse<List<UserCourseStageProgress>> getCourseProgress(
            @PathVariable Long courseId,
            @PathVariable String userId
    ) {
        log.info(
                "Fetching course progress | userId={}, courseId={}",
                userId, courseId
        );
        List<UserCourseStageProgress> progress =
                courseProgressService.getStageProgress(
                        userId, courseId
                );
        return new SharkdomApiResponse<>(
                true,
                "Course progress fetched successfully",
                progress
        );
    }

    @Operation(
            summary = "Get course progress",
            description = "Returns stage-wise progress for logged-in user"
    )
    @GetMapping("/my/partner/courses/{courseId}/progress")
    public SharkdomApiResponse<List<UserCourseStageProgress>> getCourseProgressForMyPartner(
            @PathVariable Long courseId
    ) {
        log.info(
                "Fetching course progress | courseId={}",
                courseId
        );
        List<UserCourseStageProgress> progress =
                courseProgressService.getStageProgressMyCourses(courseId);
        return new SharkdomApiResponse<>(
                true,
                "Course progress fetched successfully",
                progress
        );
    }

    @Operation(
            summary = "Save user course status",
            description = "Stores or updates course status for logged-in user"
    )
    @PostMapping("/courses/status/add")
    public SharkdomApiResponse<UserCourseStatusResponse> saveCourseStatus(
            @RequestBody UserCourseStatusRequest request
    ) {

        log.info(
                "Received course status request | userId={}, courseId={}, status={}",
                request.getUserId(),
                request.getCourseId(),
                request.getStatus()
        );

        UserCourseStatusResponse response =
                userCourseStatusService.saveStatus(request);

        return new SharkdomApiResponse<>(
                true,
                "Course status saved successfully",
                response
        );
    }

    @Operation(
            summary = "Update user course status",
            description = "Updates existing course status for logged-in user"
    )
    @PutMapping("/update/courses/status")
    public SharkdomApiResponse<UserCourseStatusResponse> updateCourseStatus(
            @RequestBody UpdateUserCourseStatusRequest request
    ) {
        log.info(
                "Received update course status request | userId={}, courseId={}, status={}",
                request.getUserId(),
                request.getCourseId(),
                request.getStatus()
        );

        UserCourseStatusResponse response =
                userCourseStatusService.updateStatus(
                        request
                );

        return new SharkdomApiResponse<>(
                true,
                "Course status updated successfully",
                response
        );
    }

    @Operation(
            summary = "Get dashboard courses",
            description = "Returns continue watching courses and paginated course list. " +
                    "If status is not provided, all courses are returned with default NOT_STARTED status."
    )
    @PostMapping("/partner/dashboard/courses")
    public SharkdomApiResponse<CourseDashboardResponse> getPartnerDashboardCourses(
            @RequestBody UserCoursePageRequest request
    ) {

        log.info(
                "Fetching dashboard courses | userId={}, status={}, page={}, size={}",
                request.getUserId(),
                request.getStatus(),
                request.getPage(),
                request.getSize()
        );

        CourseDashboardResponse response =
                userCourseStatusService.getDashboardCourses(
                        request.getUserId(),
                        request.getStatus(),   // can be null
                        request.getPage(),
                        request.getSize()
                );

        return new SharkdomApiResponse<>(
                true,
                "Dashboard courses fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Get dashboard statistics",
            description = "Returns assigned courses, completed courses, certificates and average readiness score"
    )
    @GetMapping("/dashboard/stats/users/{userId}")
    public SharkdomApiResponse<DashboardStatsResponse> getDashboardStats(
            @PathVariable String userId
    ) {

        log.info(
                "Fetching dashboard stats | userId={}",
                userId
        );

        DashboardStatsResponse response =
                dashBoardService.getDashboardStatsForPartnerPortal(userId);

        return new SharkdomApiResponse<>(
                true,
                "Dashboard stats fetched successfully",
                response
        );
    }


    @Operation(
            summary = "Assign course to partner",
            description = "Assigns a course to another organization. If already assigned, it updates the record."
    )
    @PostMapping("/my/partner/courses/assign")
    public SharkdomApiResponse<Void> assignCourseToPartner(
            @RequestBody AssignCourseRequest request
    ) {

        log.info(
                "Assigning course |  assignedOrgId={}, courseId={}",
                request.getAssignedOrgId(),
                request.getCourseId()
        );
        courseProgressService.assignCourse(
                request
        );
        return new SharkdomApiResponse<>(
                true,
                "Course assigned successfully",
                null
        );
    }

    @Operation(
            summary = "Update assigned course status",
            description = "Updates status of an assigned course using courseId and assignedOrgId"
    )
    @PostMapping("/my/partner/courses/assign/status/update")
    public SharkdomApiResponse<Void> updateAssignedCourseStatus(
            @RequestBody UpdateAssignedCourseStatusRequest request
    ) {
        log.info(
                "Updating assigned course status | courseId={}, assignedOrgId={}, status={}",
                request.getCourseId(),
                request.getAssignedOrgId(),
                request.getStatus()
        );
        courseProgressService.updateAssignedCourseStatus(request);
        return new SharkdomApiResponse<>(
                true,
                "Assigned course status updated successfully",
                null
        );
    }

    @Operation(
            summary = "Get partner assigned dashboard courses",
            description = "Returns continue watching and paginated course list " +
                    "based on assigned organization and course status"
    )
    @PostMapping("/my/partner/assigned/dashboard/courses")
    public SharkdomApiResponse<CourseDashboardResponse> getAssignedPartnerDashboardCourses(
            @RequestBody PartnerAssignedCoursePageRequest request
    ) {

        log.info(
                "Fetching partner assigned dashboard courses | assignedOrgId={}, status={}, page={}, size={}",
                request.getAssignedOrgId(),
                request.getStatus(),
                request.getPage(),
                request.getSize()
        );

        CourseDashboardResponse response =
                courseProgressService.getPartnerDashboardCourses(
                        request.getAssignedOrgId(),
                        request.getStatus(),     // can be null
                        request.getPage(),
                        request.getSize()
                );

        return new SharkdomApiResponse<>(
                true,
                "Partner assigned dashboard courses fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Get partner dashboard summary",
            description = "Returns assigned courses, completed courses, certificates and readiness score"
    )
    @GetMapping("/my/partner/dashboard/summary/{userId}")
    public SharkdomApiResponse<PartnerDashboardSummaryResponse> getPartnerDashboardSummary(
            @PathVariable Long userId
    ) {

        log.info("Fetching partner dashboard summary (static response)");

        PartnerDashboardSummaryResponse response =
                PartnerDashboardSummaryResponse.builder()
                        .assignedCourses(73)
                        .coursesCompleted(40)
                        .certificates(23)
                        .avgReadinessScore(90)
                        .build();

        return new SharkdomApiResponse<>(
                true,
                "Partner dashboard summary fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Get associated partner.",
            description = "Return the name of associated Partners name with course."
    )
    @GetMapping("/dashboard/associated/partners")
    public SharkdomApiResponse<List<String>> getMyPartnerOrganizations() {
        log.info("Fetching partner organization names assigned by logged-in org");
        List<String> partnerOrgNames =
                dashBoardService
                        .getAssociatedPartnerNames();
        return new SharkdomApiResponse<>(
                true,
                "Partner organization names fetched successfully",
                partnerOrgNames
        );
    }

    @Operation(
            summary = "Get dashboard data.",
            description = "Return the dashboard data."
    )
    @GetMapping("/dashboard/overview")
    public SharkdomApiResponse<DashboardResponse> getDashboardData() {
        log.info("Fetching partner organization names assigned by logged-in org");
        var dashboardOverview = dashBoardService
                .getDashboardOverview();
        return new SharkdomApiResponse<>(
                true,
                "Partner organization names fetched successfully",
                dashboardOverview
        );
    }

    @Operation(
            summary = "Get associated partner with course.",
            description = "Return the name of associated Partners name with course."
    )
    @GetMapping("/dashboard/associated/partners/course/{courseId}")
    public SharkdomApiResponse<List<String>> getMyPartnerOrganizationsWithCourse(
            @PathVariable Long courseId
    ) {
        log.info("Fetching partner organization names assigned by logged-in org");
        List<String> partnerOrgNames =
                dashBoardService
                        .getAssociatedPartnerNamesWithCourseId(courseId);
        return new SharkdomApiResponse<>(
                true,
                "Partner organization names fetched successfully",
                partnerOrgNames
        );
    }

    @Operation(
            summary = "Get course insights",
            description = "Returns course insights including total partners, adoption, completion, and average readiness."
    )
    @GetMapping("/dashboard/course/{courseId}/insights")
    public SharkdomApiResponse<CourseInsightsResponse> getCourseInsights(
            @PathVariable Long courseId
    ) {
        log.info("Fetching course insights for courseId: {}", courseId);
        CourseInsightsResponse response =
                userCourseStatusService.getCourseInsights(courseId);
        return new SharkdomApiResponse<>(
                true,
                "Course insights fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Verify quiz answer",
            description = "Verifies the submitted answer for a quiz based on stageId and returns whether it is correct or not"
    )
    @PostMapping("/courses/course/stage/{stageId}/quiz/answer/verify")
    public SharkdomApiResponse<Boolean> verifyQuizAnswer(
            @PathVariable Long stageId,
            @RequestParam String answer
    ) {
        log.info("Verifying quiz answer for stageId: {}", stageId);

        boolean isCorrect =
                courseService.verifyAnswer(stageId, answer);

        return new SharkdomApiResponse<>(
                true,
                "Quiz answer verified successfully",
                isCorrect
        );
    }

    @Operation(
            summary = "Get partner dashboard stats",
            description = "Returns assigned courses, completed courses, certificates and avg readiness score"
    )
    @GetMapping("/my/partner/dashboard/stats")
    public SharkdomApiResponse<PartnerDashboardStatsResponse> getDashboardStats() {

        log.info("Fetching partner dashboard stats");

        PartnerDashboardStatsResponse response =
                userCourseStatusService.getPartnerDashboardStats();

        return new SharkdomApiResponse<>(
                true,
                "Dashboard stats fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Get single course status for assigned partner",
            description = "Returns current status of a single course for assigned organization"
    )
    @GetMapping("/my/partner/courses/{courseId}/status")
    public SharkdomApiResponse<CourseStatusResponse> getSingleCourseStatus(
            @PathVariable Long courseId
    ) {
        Long assignedOrgId = Util.getOrgIdFromToken();
        log.info(
                "Fetching course status | courseId={}, assignedOrgId={}",
                courseId,
                assignedOrgId
        );
        CourseStatusResponse response =
                userCourseStatusService
                        .getAssignedOrgCourseStatus(courseId);
        log.info(
                "Course status fetched | courseId={}, status={}",
                courseId,
                response.getStatus()
        );
        return new SharkdomApiResponse<>(
                true,
                "Course status fetched successfully",
                response
        );
    }

    @Operation(
            summary = "Update course certificate URL",
            description = "Updates certificate URL of a course using courseId"
    )
    @PatchMapping("/courses/{courseId}/certificate")
    public SharkdomApiResponse<Void> updateCourseCertificateUrl(
            @PathVariable Long courseId,
            @RequestBody UpdateCourseCertificateUrlRequest request
    ) {
        log.info(
                "Updating certificate URL | courseId={}, certificateUrl={}",
                courseId, request.getCertificateUrl()
        );
        courseService.updateCourseCertificateUrl(
                courseId,
                request.getCertificateUrl()
        );
        return new SharkdomApiResponse<>(
                true,
                "Course certificate URL updated successfully",
                null
        );
    }

    @Operation(
            summary = "Get certificates by userId",
            description = "Returns list of certificates issued to a user"
    )
    @GetMapping("/users/{userId}")
    public SharkdomApiResponse<List<CourseCertificateResponse>>
    getCertificatesByUserId(
            @PathVariable String userId
    ) {
        log.info("Fetching certificates for userId={}", userId);
        List<CourseCertificateResponse> certificates =
                courseProgressService.getCertificatesByUserId(userId);
        String message = certificates.isEmpty()
                ? "No certificates found"
                : "Certificates fetched successfully";
        return new SharkdomApiResponse<>(
                true,
                message,
                certificates
        );
    }

    @Operation(
            summary = "Get certificates by org",
            description = "Returns list of certificates issued under an organization"
    )
    @GetMapping("/my/courses")
    public SharkdomApiResponse<List<CourseCertificateResponse>>
    getCertificatesByOrg() {
        List<CourseCertificateResponse> certificates =
                courseProgressService.getCertificatesByOrgId();
        String message = certificates.isEmpty()
                ? "No certificates found"
                : "Certificates fetched successfully";
        return new SharkdomApiResponse<>(
                true,
                message,
                certificates
        );
    }

    @Operation(
            summary = "Update course publish status",
            description = "Publish or unpublish a course using courseId"
    )
    @PutMapping("/courses/{courseId}/publish")
    public SharkdomApiResponse<Boolean> updateCoursePublishStatus(
            @PathVariable Long courseId,
            @RequestParam Boolean published
    ) {
        log.info("Updating publish status for courseId={} to {}", courseId, published);

        courseService.updatePublishStatus(courseId, published);

        String message = published
                ? "Course published successfully"
                : "Course unpublished successfully";

        return new SharkdomApiResponse<>(
                true,
                message,
                true
        );
    }

    @Operation(
            summary = "Get unpublished courses by organization",
            description = "Returns unpublished courses created by an organization"
    )
    @GetMapping("/courses/unpublished")
    public SharkdomApiResponse<List<Course>> getUnpublishedCoursesByOrganization(
    ) {
        Long organizationId=Util.getOrgIdFromToken();
        log.info("Fetching unpublished courses for organizationId={}", organizationId);
        List<Course> courses =
                courseService.getUnpublishedCoursesByOrganization(organizationId);
        String message = courses.isEmpty()
                ? "No unpublished courses found"
                : "Unpublished courses fetched successfully";
        return new SharkdomApiResponse<>(
                true,
                message,
                courses
        );
    }

    @GetMapping("/created/by/org/certificates")
    public SharkdomApiResponse<List<CourseCertificateResponseDTO>>
    getCoursesByCreatedOrg() {
        return courseService.getCoursesByCreatedOrg();
    }

    @PostMapping(value = "/drive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SharkdomApiResponse<DriveUploadResult>> uploadDriveFile(
            @RequestBody DriveUploadRequest driveUploadRequest
    ) {
        String driveUrl = driveUploadRequest.getDriveLink();
        log.info("Received request to upload Google Drive file: {}", driveUrl);

        if (driveUrl == null || driveUrl.isBlank()) {
            log.warn("Drive URL is missing in request payload");
            throw new ServiceException(ErrorMessages.SH166); // Invalid Google Drive link format
        }

        try {
            var driveUploadResult = driveToGcpService.validateAndUpload(driveUrl);
            return ResponseEntity.ok(
                    new SharkdomApiResponse<>(true, "File uploaded successfully", driveUploadResult)
            );
        } catch (ServiceException ex) {
            log.error("ServiceException while uploading Drive file: {}", ex.getMessage());
            return ResponseEntity.status(ex.getErrorMessage().getHttpStatus())
                    .body(new SharkdomApiResponse<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("Unexpected error while uploading Drive file", ex);
            return ResponseEntity.status(500)
                    .body(new SharkdomApiResponse<>(false, ErrorMessages.SH168.getMessage(), null));
        }
    }


}
