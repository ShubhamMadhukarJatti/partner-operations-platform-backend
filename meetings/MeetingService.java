package com.sharkdom.service.meetings;


import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.meeting.MeetingApps;
import com.sharkdom.constants.meeting.MeetingEventDuration;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.meetings.*;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.mapper.meeting.MeetingEventMapper;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.meetings.CreateMeetingModel;
import com.sharkdom.model.meetings.EventRequest;
import com.sharkdom.model.meetings.MeetingEventResponse;
import com.sharkdom.model.meetings.OrganizationSchedule;
import com.sharkdom.repository.meetings.*;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.util.Util;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Slf4j
public class MeetingService {
    @Value("${app.environment.proxy_url}")
    String baseUrl;
    private final MeetingRepository meetingRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final EmailService emailService;
    private final MeetingDetailsRepository meetingDetailsRepository;
    private final VideoSdkService videoSdkService;
    private final OrganizationRepository organizationRepository;
    private final WebhookDataRepository webhookDataRepository;
    private final ScheduleRepository scheduleRepository;
    private final GoogleMeetService googleMeetService;
    private final IntegrationRepository integrationRepository;
    private final MeetingCallbackRepository callbackRepository;
    private final WebSocketHandler webSocketHandler;
    private final NotificationService notificationService;
    private final MeetingScheduleSettingRepository meetingScheduleSettingRepository;
    private final MeetingEventRepository meetingEventRepository;

    public MeetingService(MeetingRepository meetingRepository, OrganizationCollaborationRepository organizationCollaborationRepository, EmailService emailService, MeetingDetailsRepository meetingDetailsRepository, VideoSdkService videoSdkService, OrganizationRepository organizationRepository, WebhookDataRepository webhookDataRepository, ScheduleRepository scheduleRepository, GoogleMeetService googleMeetService, IntegrationRepository integrationRepository, MeetingCallbackRepository callbackRepository, WebSocketHandler webSocketHandler, NotificationService notificationService, MeetingScheduleSettingRepository meetingScheduleSettingRepository, MeetingEventRepository meetingEventRepository) {
        this.meetingRepository = meetingRepository;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.emailService = emailService;
        this.meetingDetailsRepository = meetingDetailsRepository;
        this.videoSdkService = videoSdkService;
        this.organizationRepository = organizationRepository;
        this.webhookDataRepository = webhookDataRepository;
        this.scheduleRepository = scheduleRepository;
        this.googleMeetService = googleMeetService;
        this.integrationRepository = integrationRepository;
        this.callbackRepository = callbackRepository;
        this.webSocketHandler = webSocketHandler;
        this.notificationService = notificationService;
        this.meetingScheduleSettingRepository = meetingScheduleSettingRepository;
        this.meetingEventRepository = meetingEventRepository;
    }

    @Transactional
    public ResponseEntity<String> saveMeetingDetails(Meeting meeting) {
        meetingRepository.save(meeting);
        return ResponseEntity.ok("Saved");
    }

    @Transactional
    public ResponseEntity<List<Meeting>> getMeetingDetails(Long organizationA, Long organizationB) {
        var response = meetingRepository.findByOrganizationAAndOrganizationB(organizationA, organizationB);

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<MeetingDetails> create(CreateMeetingModel createMeetingModel) {
        Optional<OrganizationCollaboration> organizationCollaboration = organizationCollaborationRepository.findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(createMeetingModel.getSenderOrganizationId(), createMeetingModel.getReceiverOrganizationId());
        if (organizationCollaboration.isEmpty()) {
            throw new ResourceNotFoundException(ErrorMessages.SH17);
        }
        Organization senderOrganization = organizationRepository.findById(organizationCollaboration.get().getSenderOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH18));
        Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.get().getReceiverOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH19));
        MeetingDetails meetingDetails = MeetingDetails.builder()
                .senderOrganizationId(createMeetingModel.getSenderOrganizationId())
                .receiverOrganizationId(createMeetingModel.getReceiverOrganizationId())
                .title(createMeetingModel.getTitle())
                .availability(createMeetingModel.getAvailability())
                .organizationCollaborationId(organizationCollaboration.get().getId())
                .description(createMeetingModel.getDescription())
                .status("PENDING_RECEIVER")
                .build();
        var response = meetingDetailsRepository.save(meetingDetails);
        List<Schedule> senderSchedules = Optional.ofNullable(senderOrganization.getSchedules()).orElse(new ArrayList<>());
        List<Schedule> receiverSchedules = Optional.ofNullable(receiverOrganization.getSchedules()).orElse(new ArrayList<>());
        senderSchedules.add(new Schedule(meetingDetails.getId(), null, receiverOrganization.getId(), meetingDetails.getTitle(), meetingDetails.getDescription(), "PENDING_RECEIVER", null, "PENDING_RECEIVER"));
        receiverSchedules.add(new Schedule(meetingDetails.getId(), null, senderOrganization.getId(), meetingDetails.getTitle(), meetingDetails.getDescription(), "PENDING_RECEIVER", null, "PENDING_RECEIVER"));
        receiverOrganization.setSchedules(receiverSchedules);
        senderOrganization.setSchedules(senderSchedules);

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<String> acceptMeeting(CreateMeetingModel createMeetingModel) {
        Optional<MeetingDetails> meetingDetailsOptional = meetingDetailsRepository.findById(createMeetingModel.getId());
        if (meetingDetailsOptional.isPresent()) {
            MeetingDetails meetingDetails = meetingDetailsOptional.get();
            OrganizationCollaboration organizationCollaboration = organizationCollaborationRepository.findById(meetingDetails.getOrganizationCollaborationId()).get();
            Organization senderOrganization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).get();
            Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).get();
            String roomId = videoSdkService.generateMeeting();
            String meetingLink = baseUrl + "meeting/" + roomId;
            meetingDetails.setMeetingTime(createMeetingModel.getMeetingTime());
            meetingDetails.setRoomId(roomId);
            meetingDetails.setStatus("ACTIVE");
            meetingDetails.setScheduledBy(receiverOrganization.getId());
            meetingDetails.setMeetingLink(meetingLink);
            List<Schedule> senderSchedules = Optional.ofNullable(senderOrganization.getSchedules()).orElse(new ArrayList<>());
            List<Schedule> receiverSchedules = Optional.ofNullable(receiverOrganization.getSchedules()).orElse(new ArrayList<>());
            senderSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("ACTIVE");
                        schedule.setTime(createMeetingModel.getMeetingTime());
                        schedule.setMeetingLink(meetingLink);
                        schedule.setMeetingStatus("ACTIVE");
                    });
            receiverSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("ACTIVE");
                        schedule.setTime(createMeetingModel.getMeetingTime());
                        schedule.setMeetingLink(meetingLink);
                        schedule.setMeetingStatus("ACTIVE");
                    });
            receiverOrganization.setSchedules(receiverSchedules);
            senderOrganization.setSchedules(senderSchedules);
            String calendarLink = getCalendarLink(meetingDetails.getTitle(), meetingDetails.getDescription(), createMeetingModel.getMeetingTime(), meetingLink);
            var savedMeeting = meetingDetailsRepository.save(meetingDetails);
            SimpleDateFormat formatter = new SimpleDateFormat("d'th' MMM yyyy hh:mm:ss a");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String formattedDate = formatter.format(savedMeeting.getMeetingTime());
            formattedDate = formattedDate.replaceFirst("1th", "1st")
                    .replaceFirst("2th", "2nd")
                    .replaceFirst("3th", "3rd");
            organizationRepository.save(senderOrganization);
            organizationRepository.save(receiverOrganization);
            WebhookData webhookData = new WebhookData();
            webhookData.setRoomId(roomId);
            webhookDataRepository.save(webhookData);
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .meetingLink(meetingLink)
                    .calendarLink(calendarLink)
                    .meetingTime(formattedDate)
                    .senderOrganizationName(senderOrganization.getName())
                    .organizationName(receiverOrganization.getName())
                    .organizationIds(List.of(receiverOrganization.getId()))
                    .templateCode("meeting_scheduled")
                    .build(), null, 1L, 1L);
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .meetingLink(meetingLink)
                    .calendarLink(calendarLink)
                    .senderOrganizationName(receiverOrganization.getName())
                    .organizationName(senderOrganization.getName())
                    .meetingTime(formattedDate)
                    .organizationIds(List.of(senderOrganization.getId()))
                    .templateCode("meeting_scheduled")
                    .build(), null, 1L, 1L);

            Notification notification = Notification.builder()
                    .subject("Meeting Scheduled")
                    .body(String.format("Meeting have been scheduled with %s at %s", senderOrganization.getName(), formattedDate))
                    .forWeb(true)
                    .organizationId(receiverOrganization.getId())
                    .build();
            webSocketHandler.sendMessageToUser(receiverOrganization.getId(), notification);

            Notification senderNotification = Notification.builder()
                    .subject("Meeting Scheduled")
                    .body(String.format("You have scheduled a meeting with %s at %s", receiverOrganization.getName(), formattedDate)).forWeb(true)
                    .organizationId(senderOrganization.getId())
                    .build();
            webSocketHandler.sendMessageToUser(senderOrganization.getId(), senderNotification);
            notificationService.create(notification);
            notificationService.create(senderNotification);

        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH20);
        }
        return ResponseEntity.ok("Saved");
    }

    @Transactional
    public ResponseEntity<String> rescheduleMeeting(CreateMeetingModel createMeetingModel) {
        Optional<MeetingDetails> meetingDetailsOptional = meetingDetailsRepository.findById(createMeetingModel.getId());
        if (meetingDetailsOptional.isPresent()) {
            MeetingDetails meetingDetails = meetingDetailsOptional.get();
            OrganizationCollaboration organizationCollaboration = organizationCollaborationRepository.findById(meetingDetails.getOrganizationCollaborationId()).get();
            Organization senderOrganization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).get();
            Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).get();
            String roomId = videoSdkService.generateMeeting();
            String meetingLink = baseUrl + "meeting/" + roomId;
            MeetingDetails rescheduleMeeting = MeetingDetails.builder()
                    .senderOrganizationId(meetingDetails.getSenderOrganizationId())
                    .receiverOrganizationId(meetingDetails.getReceiverOrganizationId())
                    .title(meetingDetails.getTitle())
                    .organizationCollaborationId(meetingDetails.getOrganizationCollaborationId())
                    .description(meetingDetails.getDescription())
                    .status("ACTIVE")
                    .meetingTime(createMeetingModel.getMeetingTime())
                    .roomId(roomId)
                    .meetingLink(meetingLink)
                    .build();
            meetingDetails.setStatus("RESCHEDULED");
            meetingDetails.setRescheduledBy(createMeetingModel.getRescheduledBy());
            videoSdkService.deactivateMeeting(meetingDetails.getRoomId());
            List<Schedule> senderSchedules = Optional.ofNullable(senderOrganization.getSchedules()).orElse(new ArrayList<>());
            List<Schedule> receiverSchedules = Optional.ofNullable(receiverOrganization.getSchedules()).orElse(new ArrayList<>());
            senderSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("ACTIVE");
                        schedule.setTime(createMeetingModel.getMeetingTime());
                        schedule.setMeetingLink(meetingLink);
                        schedule.setMeetingStatus("RESCHEDULED");
                    });
            receiverSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("ACTIVE");
                        schedule.setTime(createMeetingModel.getMeetingTime());
                        schedule.setMeetingLink(meetingLink);
                        schedule.setMeetingStatus("RESCHEDULED");
                    });
            receiverOrganization.setSchedules(receiverSchedules);
            senderOrganization.setSchedules(senderSchedules);
            meetingDetailsRepository.save(meetingDetails);
            meetingDetailsRepository.save(rescheduleMeeting);
            organizationRepository.save(senderOrganization);
            organizationRepository.save(receiverOrganization);
            WebhookData webhookData = new WebhookData();
            webhookData.setRoomId(roomId);
            webhookDataRepository.save(webhookData);
            String calendarLink = getCalendarLink(meetingDetails.getTitle(), meetingDetails.getDescription(), createMeetingModel.getMeetingTime(), meetingLink);
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .meetingLink(rescheduleMeeting.getMeetingLink())
                    .calendarLink(calendarLink)
                    .meetingTime(createMeetingModel.getMeetingTime().toString())
                    .organizationName(receiverOrganization.getName())
                    .senderOrganizationName(organizationRepository.findNameById(createMeetingModel.getRescheduledBy()))
                    .organizationIds(List.of(receiverOrganization.getId()))
                    .templateCode("meeting_reschule")
                    .build(), null, 1L, 1L);

            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .meetingLink(rescheduleMeeting.getMeetingLink())
                    .calendarLink(calendarLink)
                    .meetingTime(createMeetingModel.getMeetingTime().toString())
                    .organizationName(senderOrganization.getName())
                    .senderOrganizationName(organizationRepository.findNameById(createMeetingModel.getRescheduledBy()))
                    .organizationIds(List.of(senderOrganization.getId()))
                    .templateCode("meeting_reschule")
                    .build(), null, 1L, 1L);
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH20);
        }
        return ResponseEntity.ok("Saved");
    }

    public List<OrganizationSchedule> getOrganizationSchedule(Long organizationId, Long scheduledWith) {
        var organization = organizationRepository.findById(organizationId);
        if (organization.isPresent()) {
            var res = Optional.ofNullable(organization.get().getSchedules()).orElse(new ArrayList<>());
            var filteredSchedule = res.stream().filter(schedule -> schedule.getScheduledWith().equals(scheduledWith)).toList();
            List<OrganizationSchedule> organizationSchedules = new ArrayList<>();
            filteredSchedule.forEach(schedule -> {
                MeetingDetails meetingDetails = meetingDetailsRepository.findById(schedule.getMeetingDetailsId()).get();
                OrganizationSchedule organizationSchedule = OrganizationSchedule.builder()
                        .id(schedule.getId())
                        .creationTimestamp(schedule.getCreationTimestamp())
                        .lastUpdatedTimestamp(schedule.getLastUpdatedTimestamp())
                        .time(schedule.getTime())
                        .meetingDetailsId(schedule.getMeetingDetailsId())
                        .scheduledWith(schedule.getScheduledWith())
                        .title(schedule.getTitle())
                        .description(schedule.getDescription())
                        .status(schedule.getStatus())
                        .meetingLink(schedule.getMeetingLink())
                        .meetingStatus(schedule.getMeetingStatus())
                        .senderOrganizationId(meetingDetails.getSenderOrganizationId())
                        .receiverOrganizationId(meetingDetails.getReceiverOrganizationId())
                        .meetingTime(meetingDetails.getMeetingTime())
                        .roomId(meetingDetails.getRoomId())
                        .organizationCollaborationId(meetingDetails.getOrganizationCollaborationId())
                        .scheduledBy(meetingDetails.getScheduledBy())
                        .rescheduledBy(meetingDetails.getRescheduledBy())
                        .cancelledBy(meetingDetails.getCancelledBy())
                        .availability(meetingDetails.getAvailability())
                        .build();
                organizationSchedules.add(organizationSchedule);
            });
            return organizationSchedules;
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH21);
        }
    }

    public WebhookData getRoomDetails(String roomId) {
        return webhookDataRepository.findByRoomId(roomId);
    }

    public Optional<MeetingDetails> getMeetingDetails(Long meetingId) {
        return meetingDetailsRepository.findById(meetingId);
    }

    public void saveCallbackData(Map data) {
        try {
            String webhookType = data.get("webhookType").toString();
            Map<String, Object> dataMap = (Map<String, Object>) data.get("data");
            String meetingId = (String) dataMap.get("meetingId");
            WebhookData webhookData = webhookDataRepository.findByRoomId(meetingId);
            if (webhookData != null) {
                String sessionId = (String) dataMap.get("sessionId");
                if ("participant-joined".equalsIgnoreCase(webhookType)) {
                    String participantId = (String) dataMap.get("participantId");
                    String participantName = (String) dataMap.get("participantName");
                    List<Participant> participants = Optional.ofNullable(webhookData.getParticipants()).orElse(new ArrayList<>());
                    Participant participant = new Participant();
                    participant.setParticipantId(participantId);
                    participant.setParticipantName(participantName);
                    participant.setJoinedAt(DateTime.now().toDate());
                    participant.setSessionId(sessionId);
                    participants.add(participant);
                    webhookData.setParticipants(participants);
                    webhookDataRepository.save(webhookData);
                } else if ("participant-left".equalsIgnoreCase(webhookType)) {
                    String participantId = (String) dataMap.get("participantId");
                    List<Participant> participants = Optional.ofNullable(webhookData.getParticipants()).orElse(new ArrayList<>());
                    participants.stream()
                            .filter(participant -> participantId.equals(participant.getParticipantId()))
                            .findFirst()
                            .ifPresent(participant -> participant.setLeftAt(DateTime.now().toDate()));
                    webhookData.setParticipants(participants);
                    webhookDataRepository.save(webhookData);
                } else if ("session-started".equalsIgnoreCase(webhookType)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String startDate = (String) dataMap.get("start");
                    Date start = dateFormat.parse(startDate);
                    List<Session> sessions = Optional.ofNullable(webhookData.getSessions()).orElse(new ArrayList<>());
                    Session session = new Session();
                    session.setSessionId(sessionId);
                    session.setStart(start);
                    sessions.add(session);
                    webhookData.setSessions(sessions);
                    webhookDataRepository.save(webhookData);
                } else if ("session-ended".equalsIgnoreCase(webhookType)) {
                    MeetingDetails meetingDetails = meetingDetailsRepository.findByRoomId(meetingId);
                    meetingDetails.setStatus("COMPLETED");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String endDate = (String) dataMap.get("end");
                    Date end = dateFormat.parse(endDate);
                    List<Session> sessions = Optional.ofNullable(webhookData.getSessions()).orElse(new ArrayList<>());
                    sessions.stream()
                            .filter(session -> sessionId.equals(session.getSessionId()))
                            .findFirst()
                            .ifPresent(session -> session.setEnd(end));
                    webhookData.setSessions(sessions);
                    List<Schedule> schedules = scheduleRepository.findAllByMeetingDetailsId(meetingDetails.getId());
                    schedules.forEach(schedule -> {
                        schedule.setStatus("COMPLETED");
                        schedule.setMeetingStatus("COMPLETED");
                    });
                    scheduleRepository.saveAll(schedules);
                    meetingDetailsRepository.save(meetingDetails);
                    webhookDataRepository.save(webhookData);
                }
            }
        } catch (Exception e) {
            log.error("unable to save callback {}", e.getMessage());
        }
    }

    public void saveWebhookData(Map data) {
        var callback = CallbackData.builder().data(data.toString()).build();
        callbackRepository.save(callback);
    }

    private String getCalendarLink(String title, String description, Date time, String meetingUrl) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startTime = sdf.format(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.MINUTE, 30);
        Date endTimeDate = calendar.getTime();
        String endTime = sdf.format(endTimeDate);
        return "https://www.google.com/calendar/render?action=TEMPLATE&text=" + title + "&dates=" + startTime + "/" + endTime + "&details=" + description + "&location=" + meetingUrl;

    }

    public MeetingDetails cancelMeeting(CreateMeetingModel createMeetingModel) {
        Optional<MeetingDetails> meetingDetailsOptional = meetingDetailsRepository.findById(createMeetingModel.getId());
        if (meetingDetailsOptional.isPresent()) {
            MeetingDetails meetingDetails = meetingDetailsOptional.get();
            OrganizationCollaboration organizationCollaboration = organizationCollaborationRepository.findById(meetingDetails.getOrganizationCollaborationId()).get();
            Organization senderOrganization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).get();
            Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).get();
            videoSdkService.deactivateMeeting(meetingDetails.getRoomId());
            meetingDetails.setStatus("CANCELLED");
            meetingDetails.setCancelledBy(createMeetingModel.getCancelledBy());
            List<Schedule> senderSchedules = Optional.ofNullable(senderOrganization.getSchedules()).orElse(new ArrayList<>());
            List<Schedule> receiverSchedules = Optional.ofNullable(receiverOrganization.getSchedules()).orElse(new ArrayList<>());
            senderSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("CANCELLED");
                        schedule.setMeetingStatus("CANCELLED");
                    });
            receiverSchedules.stream()
                    .filter(schedule -> schedule.getMeetingDetailsId().equals(meetingDetails.getId()))
                    .findFirst()
                    .ifPresent(schedule -> {
                        schedule.setStatus("CANCELLED");
                        schedule.setMeetingStatus("CANCELLED");
                    });
            receiverOrganization.setSchedules(receiverSchedules);
            senderOrganization.setSchedules(senderSchedules);

            organizationRepository.save(senderOrganization);
            organizationRepository.save(receiverOrganization);
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .organizationIds(List.of(receiverOrganization.getId()))
                    .senderOrganizationName(organizationRepository.findNameById(createMeetingModel.getCancelledBy()))
                    .organizationName(receiverOrganization.getName())
                    .templateCode("meeting_cancelled")
                    .build(), null, 1L, 1L);

            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .organizationIds(List.of(senderOrganization.getId()))
                    .templateCode("meeting_cancelled")
                    .senderOrganizationName(organizationRepository.findNameById(createMeetingModel.getCancelledBy()))
                    .organizationName(senderOrganization.getName())
                    .build(), null, 1L, 1L);
            return meetingDetailsRepository.save(meetingDetails);
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH20);
        }
    }

    public void generateCalendlyToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://auth.calendly.com/oauth/token"; // Replace with your URL
        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
        postData.add("grant_type", "authorization_code");
        postData.add("code", "your_code"); // Replace with the actual code
        postData.add("redirect_uri", "https://spiceshing.com/");
        postData.add("client_id", "oHKfv8dx02kzkoA0G8aV3gjGVYosObRVhKw1K91dX_U"); // Replace with your client ID
        postData.add("client_secret", "yxe889u_TZlrQiVyt_JaG57K9dUFlQl_e44AzDQxzWns"); // Replace with your client secret

        // Set headers (optional, but recommended)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic b0hLZnY4ZHgwMmt6a29BMEc4YVYzZ2pHVllvc09iUlZoS3cxSzkxZFhfVTp5eGU4ODl1X1RabHJRaVZ5dF9KYUc1N0s5ZFVGbFFsX2U0NEF6RFF4elducw==");
        // Create the request entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);

        // Send POST request
        // ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        // Print the response (for debugging purposes)
        //System.out.println(response.getBody());
        String clientId = "oHKfv8dx02kzkoA0G8aV3gjGVYosObRVhKw1K91dX_U"; // Replace with your client ID
        String clientSecret = "yxe889u_TZlrQiVyt_JaG57K9dUFlQl_e44AzDQxzWns"; // Replace with your client secret
        String grantType = "client_credentials"; // or "authorization_code" based on your flow

        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("Authorization", "Basic b0hLZnY4ZHgwMmt6a29BMEc4YVYzZ2pHVllvc09iUlZoS3cxSzkxZFhfVTp5eGU4ODl1X1RabHJRaVZ5dF9KYUc1N0s5ZFVGbFFsX2U0NEF6RFF4elducw==");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("Response: " + response.getBody());
    }

    @Transactional
    public Map<String, String> createGoogleMeet(EventRequest eventRequest) {
        var token = integrationRepository.findByOrganizationIdAndIntegrationType(eventRequest.getSenderOrganizationId(), IntegrationType.G_CALENDAR).getRefreshToken();
        var senderEmail = organizationRepository.findEmailById(eventRequest.getSenderOrganizationId());
        var receiverEmail = organizationRepository.findEmailById(eventRequest.getReceiverOrganizationId());
        try {
            String eventLink = googleMeetService.createEvent(token, eventRequest.getTitle(), eventRequest.getDescription(), eventRequest.getStartDateTime(), eventRequest.getEndDateTime(), List.of(senderEmail, receiverEmail));
            saveMeetingEvent(eventRequest, eventLink);
            return Map.of("eventLink", eventLink);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_FORBIDDEN) {
                String reason = e.getDetails().getErrors().get(0).getReason();
                if ("ACCESS_TOKEN_SCOPE_INSUFFICIENT".equals(reason)) {
                    throw new SharkdomException(ErrorMessages.SH155);
                } else {
                    throw new SharkdomException(ErrorMessages.SH156, reason);
                }
            } else {
                throw new SharkdomException(ErrorMessages.SH157, e.getDetails().getMessage());
            }
        } catch (IOException e) {
            throw new SharkdomException(ErrorMessages.SH158, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getLocalizedMessage());
        }
    }

    private void saveMeetingEvent(EventRequest eventRequest, String eventLink) {
        MeetingEvent meetingEvent = extractedEventRequest(eventRequest, eventLink);
        MeetingEvent savedMeetingEvent = meetingEventRepository.save(meetingEvent);
        log.info("Meeting Event saved to database: {}", savedMeetingEvent);
    }

    private static MeetingEvent extractedEventRequest(EventRequest eventRequest, String eventLink) {
        return MeetingEvent.builder()
                .meetLink(eventLink)
                .meetingApp(MeetingApps.G_CALENDAR)
                .receiverOrganizationId(eventRequest.getReceiverOrganizationId())
                .senderOrganizationId(eventRequest.getSenderOrganizationId())
                .meetDate(LocalDate.now())
                .startDateTime(parseStringToLocalDateTime(eventRequest.getStartDateTime()))
                .endDateTime(parseStringToLocalDateTime(eventRequest.getEndDateTime()))
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .status("active")
                .build();
    }

    private static LocalDateTime parseStringToLocalDateTime(String timestamp) {
        log.info("Parsing timestamp: {}", timestamp);
        try {
            OffsetDateTime odt = OffsetDateTime.parse(timestamp);
            return odt.toLocalDateTime();
        } catch (Exception exception) {
            log.error("Error: {}", exception.getMessage());
            throw new ServiceException(ErrorMessages.SH116, exception.getMessage());
        }
    }


    @Scheduled(fixedRate = 300000)
    public void sendMeetingReminders() {
        log.error("insider reminder");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(55);
        LocalDateTime endTime = now.plusMinutes(65);
        List<MeetingDetails> upcomingMeetings = meetingDetailsRepository.findMeetingsWithinTimeRange(startTime, endTime);
        for (MeetingDetails meeting : upcomingMeetings) {
            var senderName = organizationRepository.findNameById(meeting.getSenderOrganizationId());
            var receiverName = organizationRepository.findNameById(meeting.getReceiverOrganizationId());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a"); // "hh" for 12-hour clock, "a" for AM/PM
            String timeOnly = timeFormat.format(meeting.getMeetingTime());
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .calendarLink(meeting.getMeetingLink())
                    .meetingTime(timeOnly)
                    .senderOrganizationName(senderName)
                    .organizationName(receiverName)
                    .organizationIds(List.of(meeting.getReceiverOrganizationId()))
                    .templateCode("meeting_link_now")
                    .build(), null, 1L, 1L);
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                    .calendarLink(meeting.getMeetingLink())
                    .senderOrganizationName(receiverName)
                    .organizationName(senderName)
                    .meetingTime(timeOnly)
                    .organizationIds(List.of(meeting.getSenderOrganizationId()))
                    .templateCode("meeting_link_now")
                    .build(), null, 1L, 1L);
            meeting.setReminderSent(true);
            meetingDetailsRepository.save(meeting);
        }
    }

    @Transactional
    public MeetingScheduleSettings updateMeetingScheduleSettings(@Valid MeetingScheduleSettings meetingScheduleSettings) {
        try {
            Long orgIdFromToken = Util.getOrgIdFromToken();
            if (ObjectUtils.isEmpty(meetingScheduleSettings.getOrganizationId())) {
                meetingScheduleSettings.setOrganizationId(orgIdFromToken);
            }
            Optional<MeetingScheduleSettings> foundMeetingScheduleSettings = meetingScheduleSettingRepository.findByOrganizationId(orgIdFromToken);
            return foundMeetingScheduleSettings.map(scheduleSettings -> updateFoundMeetingScheduleSettings(scheduleSettings.getId(), meetingScheduleSettings))
                    .orElseGet(() -> addMeetingScheduleSettings(meetingScheduleSettings));
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    private MeetingScheduleSettings addMeetingScheduleSettings(@Valid MeetingScheduleSettings meetingScheduleSettings) {
        MeetingScheduleSettings savedMeetingScheduleSetting = meetingScheduleSettingRepository.save(meetingScheduleSettings);
        log.info("Meeting Schedule Setting Saved: {}", savedMeetingScheduleSetting);
        return savedMeetingScheduleSetting;
    }

    private MeetingScheduleSettings updateFoundMeetingScheduleSettings(Long id, @Valid MeetingScheduleSettings meetingScheduleSettings) {
        meetingScheduleSettings.setId(id);
        MeetingScheduleSettings updatedMeetingScheduleSettings = addMeetingScheduleSettings(meetingScheduleSettings);
        log.info("Updated Meeting Schedule Settings: {}", updatedMeetingScheduleSettings);
        return updatedMeetingScheduleSettings;
    }

    @Transactional
    public MeetingScheduleSettings getMeetingScheduleSettingByOrganizationId(Long organizationId) {
        MeetingScheduleSettings foundMeetingScheduleSettings = meetingScheduleSettingRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH159, organizationId));
        foundMeetingScheduleSettings.setConnectedApps(addIntegratedApps(organizationId));
        log.info("Found Meeting Schedule Settings with organizationId: {}", foundMeetingScheduleSettings);
        return foundMeetingScheduleSettings;
    }

    private List<MeetingApps> addIntegratedApps(Long organizationId) {
        List<MeetingApps> connectedApps = new ArrayList<>();
        integrationRepository.findAllByOrganizationId(organizationId).forEach(integration -> {
            if (Objects.nonNull(integration.getIntegrationType())) {
                String type = integration.getIntegrationType().toString();
                try {
                    connectedApps.add(MeetingApps.valueOf(type));
                } catch (IllegalArgumentException e) {
                    log.error("Error while finding connected app for given organization: {}", e.getMessage());
                }
            }
        });
        return connectedApps;
    }

    @Transactional
    public List<MeetingEventResponse> getMeetingEventByDuration(MeetingEventDuration meetingEventDuration) {
        try {
            return switch (meetingEventDuration) {
                case TODAY -> getMeetingEventDetailForToday();
                case TOMORROW -> getMeetingEventDetailForTomorrow();
                case WEEK -> getMeetingEventDetailForWeek();
                case MONTH -> getMeetingEventDetailForMonth();
            };
        } catch (Exception e) {
            log.info("Error: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    private List<MeetingEventResponse> getMeetingEventDetailForMonth() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        final Long orgIdFromToken = Util.getOrgIdFromToken();
        return meetingEventRepository.findByOrganizationMeetDateBetween(orgIdFromToken, start, end)
                .stream()
                .map(meetingEvent -> MeetingEventMapper.mapMeetingEventToMeetingEventResponse(meetingEvent,
                        getOrganization(meetingEvent.getReceiverOrganizationId()),
                        getOrganization(meetingEvent.getSenderOrganizationId())))
                .toList();
    }
    private List<MeetingEventResponse> getMeetingEventDetailForWeek() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(6);
        final Long orgIdFromToken = Util.getOrgIdFromToken();
        return meetingEventRepository.findByOrganizationMeetDateBetween(orgIdFromToken, start, end)
                .stream()
                .map(meetingEvent -> MeetingEventMapper.mapMeetingEventToMeetingEventResponse(meetingEvent,
                        getOrganization(meetingEvent.getReceiverOrganizationId()),
                        getOrganization(meetingEvent.getSenderOrganizationId())))
                .toList();
    }

    private List<MeetingEventResponse> getMeetingEventDetailForTomorrow() {
        final Long orgIdFromToken = Util.getOrgIdFromToken();
        return meetingEventRepository.findByOrganizationMeetDate(orgIdFromToken, LocalDate.now().plusDays(1))
                .stream()
                .map(meetingEvent -> MeetingEventMapper.mapMeetingEventToMeetingEventResponse(meetingEvent,
                        getOrganization(meetingEvent.getReceiverOrganizationId()),
                        getOrganization(meetingEvent.getSenderOrganizationId())))
                .toList();
    }

    private List<MeetingEventResponse> getMeetingEventDetailForToday() {
        final Long orgIdFromToken = Util.getOrgIdFromToken();
        return meetingEventRepository.findByOrganizationMeetDate(orgIdFromToken, LocalDate.now())
                .stream()
                .map(meetingEvent -> MeetingEventMapper.mapMeetingEventToMeetingEventResponse(meetingEvent,
                        getOrganization(meetingEvent.getReceiverOrganizationId()),
                        getOrganization(meetingEvent.getSenderOrganizationId())))
                .toList();
    }

    private Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH22));
    }

}
