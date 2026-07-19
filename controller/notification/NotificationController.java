package com.sharkdom.controller.notification;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.model.notification.NotificationRequest;
import com.sharkdom.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController

@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Update an existing notification by sending organization json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the notification and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "notification not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("")
    public ResponseEntity<Notification> update(@RequestBody Notification notification) throws Exception {
        return ResponseEntity.ok(notificationService.update(notification));
    }

    @Operation(summary = "Create new notification by sending notification json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New notification created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.create(notification));
    }

    @Operation(summary = "Get notifications by its userid, returns Page<Notification>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the notification.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "Notification not found with given userId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/userId")
    public ResponseEntity<Page<Notification>> findByUserId(@RequestParam(name = "userId") String userId,
                                                           @RequestParam(name = "forMobile") boolean forMobile,
                                                           @RequestParam(name = "forWeb") boolean forWeb,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(notificationService.findAllByUserId(userId, forMobile, forWeb, page, size));
    }

    @Operation(summary = "Get notifications by its organizationId, returns Page<Notification>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the notification.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "Notification not found with given organizationId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/organizationId")
    public ResponseEntity<Page<Notification>> findByOrganizationId(
            @RequestParam(name = "forMobile") boolean forMobile,
            @RequestParam(name = "forWeb") boolean forWeb,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(notificationService.findAllByOrganizationId(forMobile, forWeb, page, size));
    }

    @Operation(summary = "Get notifications for mobile by its userid, returns Page<Notification>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the notifications.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "Notification not found with given userId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/forMobileByUserId")
    public ResponseEntity<Page<Notification>> forMobileByUserId(@RequestParam(name = "userId") String userId,
                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(notificationService.findAllForMobileByUserId(userId, page, size));
    }

    @Operation(summary = "Get notifications for web by its userid, returns Page<Notification>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the notifications.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "Notification not found with given userId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/forWebByUserId")
    public ResponseEntity<Page<Notification>> forWebByUserId(@RequestParam(name = "userId") String userId,
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(notificationService.findAllForWebByUserId(userId, page, size));
    }

    @Operation(summary = "Use json patch to partially update an Notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "404", description = "Notification not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})

    @PatchMapping(path = "", consumes = "application/json-patch+json")
    public ResponseEntity<Notification> updateNotification(@RequestParam(name = "id") Long id,
                                                           @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                                   @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                           + "    {\"op\":\"replace\",\"path\":\"/subject\",\"value\":\"xyz\"},\r\n"
                                                                           + "    {\"op\":\"add\",\"path\":\"/body\",\"value\":\"abc\"}\r\n" + "]"),
                                                                   @ExampleObject(name = "metadata", value = "[{\"op\":\"remove\",\"path\":\"/briefDescription\"}]")})) @RequestBody JsonPatch patch)
            throws Exception {
        return ResponseEntity.ok(notificationService.patch(id, patch));
    }


    @Operation(summary = "Send notification to All Active Users by sending notification json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New notifications created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendToAllActiveUsers")
    public ResponseEntity<HttpStatus> sendToAllActiveUsers(@RequestBody Notification notification) {
        notificationService.sendNotificationAllActiveUsers(notification);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(summary = "Send notification to All Organizations Users by sending notification json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New notifications created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendToAllOrganizations")
    public ResponseEntity<HttpStatus> sendToAllOrganizations(@RequestBody Notification notification) {
        notificationService.sendNotificationAllOrganizations(notification);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(summary = "Unsubscribe notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unsubscribed from notification successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody NotificationRequest notificationRequest) {
        notificationService.unsubscribe(notificationRequest);
        return ResponseEntity.ok(Map.of("message", "unsubscribed successfully"));
    }

    @Operation(summary = "Subscribe notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscribed to notification successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody NotificationRequest notificationRequest) {
        notificationService.unsubscribe(notificationRequest);
        return ResponseEntity.ok(Map.of("message", "Subscribed successfully"));
    }
}
