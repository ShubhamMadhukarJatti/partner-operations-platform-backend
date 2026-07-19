package com.sharkdom.controller.meetings;

import com.sharkdom.constants.SwaggerConstants;
import com.sharkdom.constants.meeting.MeetingEventDuration;
import com.sharkdom.entity.meetings.MeetingDetails;
import com.sharkdom.entity.meetings.MeetingScheduleSettings;
import com.sharkdom.entity.meetings.WebhookData;
import com.sharkdom.model.meetings.CreateMeetingModel;
import com.sharkdom.model.meetings.EventRequest;
import com.sharkdom.model.meetings.MeetingEventResponse;
import com.sharkdom.model.meetings.OrganizationSchedule;
import com.sharkdom.service.meetings.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/meetings")
public class MeetingController {
    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @Deprecated
    @Operation(summary = "Sender Will Create Meeting",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.CREATE_MEETING))))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting created successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/create")
    public ResponseEntity<MeetingDetails> createMeeting(@RequestBody CreateMeetingModel createMeetingModel) {
        return meetingService.create(createMeetingModel);
    }

    @Deprecated
    @Operation(summary = "Receiver Will Schedule Meeting ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.ACCEPT_MEETING))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting scheduled."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/schedule")
    public ResponseEntity<String> acceptMeeting(@RequestBody CreateMeetingModel createMeetingModel) {
        return meetingService.acceptMeeting(createMeetingModel);
    }

    @Deprecated
    @Operation(summary = "Sender and Receiver can Reschedule Meeting ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.RESCHEDULE_MEETING))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting rescheduled."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/reschedule")
    public ResponseEntity<String> rescheduleMeeting(@RequestBody CreateMeetingModel createMeetingModel) {
        return meetingService.rescheduleMeeting(createMeetingModel);
    }

    @Deprecated
    @Operation(summary = "Get Organization Schedule ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Organization Schedule "),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/schedule")
    public List<OrganizationSchedule> getOrganizationSchedule(@RequestParam Long organizationId, @RequestParam Long scheduledWith) {
        return meetingService.getOrganizationSchedule(organizationId, scheduledWith);
    }

    @Deprecated
    @Operation(summary = "Get Meeting Details ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Meeting Details"),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/{meetingId}")
    public Optional<MeetingDetails> getMeetingDetails(@PathVariable Long meetingId) {
        return meetingService.getMeetingDetails(meetingId);
    }

    @Operation(summary = "Meeting Callback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/callback")
    public void meetingCallback(@RequestBody Map data) {
        log.info("callback data {}", data);
        meetingService.saveCallbackData(data);
    }

    @Deprecated
    @Operation(summary = "Room Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Room Details"),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/room/{roomId}")
    public WebhookData getRoomDetails(@PathVariable String roomId) {
        return meetingService.getRoomDetails(roomId);
    }

    @Deprecated
    @Operation(summary = "Sender and Receiver can Cancel Meeting ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.CANCEL_MEETING))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting cancelled."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/cancel")
    public ResponseEntity<MeetingDetails> cancelMeeting(@RequestBody CreateMeetingModel createMeetingModel) {
        return ResponseEntity.ok(meetingService.cancelMeeting(createMeetingModel));
    }

    @Deprecated
    @Operation(summary = "Sender and Receiver can Cancel Meeting ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.CANCEL_MEETING))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting cancelled."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/calendly")
    public void generateToken() {
        meetingService.generateCalendlyToken();
    }

    @Deprecated
    @Operation(summary = "Sender and Receiver can Cancel Meeting ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
                    examples = @ExampleObject(value = SwaggerConstants.CANCEL_MEETING))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting cancelled."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/calendar/callback")
    public void googleCalendar(@RequestBody(required = false) Map<Object, Object> request) {
       meetingService.saveCallbackData(request);
    }

    @Operation(
            summary = "create a google meet for sender and receiver organizations",
            description = "create a google meet for sender and receiver organizations",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Meeting event details.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EventRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Meeting event link",
                            content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permission to access Google Calendar.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Access denied.", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Google API error.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @PostMapping(value = "/google-meet/create")
    public ResponseEntity<Map<String, String>> googleMeet(@RequestBody EventRequest eventRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createGoogleMeet(eventRequest));
    }

    @Operation(
            summary = "Get meeting event by duration",
            description = "Fetches the meeting event for a specific duration like TODAY, TOMORROW, WEEK, MONTH.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched meeting event.",
                            content = @Content(schema = @Schema(implementation = MeetingEventResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @GetMapping(value = "/meeting-event")
    public ResponseEntity<List<MeetingEventResponse>> getMeetingEventByDuration(@RequestParam(value = "meetingEventDuration") MeetingEventDuration meetingEventDuration) {
        return ResponseEntity.status(HttpStatus.OK).body(meetingService.getMeetingEventByDuration(meetingEventDuration));
    }

    @Operation(
            summary = "Update meeting schedule settings for an organization",
            description = "Updates the meeting schedule settings like connectedApps, default apps, PreferredMeetTime like weekdays, weekends for a specific organization.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Meeting schedule settings to be updated.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Meeting schedule settings updated successfully.",
                            content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request or missing data.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @PutMapping(value = "/schedule/settings")
    public ResponseEntity<MeetingScheduleSettings> updateMeetingScheduleSettings(@RequestBody @Valid MeetingScheduleSettings meetingScheduleSettings){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(meetingService.updateMeetingScheduleSettings(meetingScheduleSettings));
    }

    @Operation(
            summary = "Update meeting schedule settings for an organization",
            description = "Updates the meeting schedule settings like connectedApps, default apps, PreferredMeetTime like weekdays, weekends for a specific organization.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Meeting schedule settings to be updated.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Meeting schedule settings updated successfully.",
                            content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request or missing data.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @PostMapping(value = "/schedule/settings")
    public ResponseEntity<MeetingScheduleSettings> saveMeetingScheduleSettings(@RequestBody @Valid MeetingScheduleSettings meetingScheduleSettings){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(meetingService.updateMeetingScheduleSettings(meetingScheduleSettings));
    }

    @Operation(
            summary = "Get meeting schedule settings by organization ID",
            description = "Fetches the current meeting schedule settings for a specific organization ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched meeting schedule settings.",
                            content = @Content(schema = @Schema(implementation = MeetingScheduleSettings.class))),
                    @ApiResponse(responseCode = "404", description = "Settings not found for the provided organization ID.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @GetMapping(value = "/schedule/settings/{organizationId}")
    public ResponseEntity<MeetingScheduleSettings> getMeetingScheduleSettingByOrganizationId(@PathVariable(value = "organizationId") Long organizationId){
        return ResponseEntity.status(HttpStatus.OK).body(meetingService.getMeetingScheduleSettingByOrganizationId(organizationId));
    }
}
