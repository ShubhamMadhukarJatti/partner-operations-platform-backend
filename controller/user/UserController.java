package com.sharkdom.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.dto.DealUserFlagResponse;
import com.sharkdom.dto.InviteStatusResponseDTO;
import com.sharkdom.dto.PmUserFlagResponse;
import com.sharkdom.dto.UserFlagResponse;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.user.SlackIntegration;
import com.sharkdom.entity.user.User;
import com.sharkdom.entity.user.UserSignupDetails;
import com.sharkdom.model.user.UserEmail;
import com.sharkdom.model.user.UserSearchResponse;
import com.sharkdom.service.user.UserService;
import com.sharkdom.service.user.UserSignupDetailsService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@RestController

@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserSignupDetailsService userSignupDetailsService;

    @Operation(summary = "Check if an username is available or not.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/isUsernameAvailable")
    public ResponseEntity<Map<String, Boolean>> isUsernameAvailable(@RequestParam(name = "username") String username) throws Exception {
        return ResponseEntity.ok(Collections.singletonMap("isUsernameAvailble", userService.isUsernameAvailable(username)));
    }

    @Operation(summary = "Get a User by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/id")
    public ResponseEntity<User> findById(@RequestParam(name = "id") long id) throws Exception {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Get a User by its userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/userId")
    public ResponseEntity<User> findByUserId(@RequestParam(name = "userId") String userId) throws Exception {
        return userService.findByUserId(userId);
    }

    @Operation(summary = "Get a User by its username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found with given username", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/username")
    public ResponseEntity<User> findByUsername(@RequestParam(name = "username") String username) throws Exception {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @Operation(summary = "Update an existing User by sending user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("")
    public ResponseEntity<User> update(@RequestBody User updated) throws Exception {
        return ResponseEntity.ok(userService.update(updated));
    }

    @Operation(summary = "Create new User by sending user json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New user created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody User created) {
        return ResponseEntity.ok(userService.create(created));
    }

    @Operation(summary = "Use json patch to partially update an user, for more details refer https://www.baeldung.com/spring-rest-json-patch ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "", consumes = "application/json-patch+json")
    public ResponseEntity<User> updateUser(@RequestParam(name = "userId") String userId,
                                           @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                   @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                           + "    {\"op\":\"replace\",\"path\":\"/mobile\",\"value\":\"+91 8744956986\"},\r\n"
                                                           + "    {\"op\":\"add\",\"path\":\"/firstName\",\"value\":\"Mike\"}\r\n" + "]"),
                                                   @ExampleObject(name = "Sending remove operation to remove briefDescription", value = "[{\"op\":\"remove\",\"path\":\"/briefDescription\"}]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request with userId: " + userId + " and json: " + patch);
        return ResponseEntity.ok(userService.patch(userId, patch));
    }

    @Operation(summary = "Update last activity time of user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity time updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/lastActivityTime")
    public ResponseEntity<HttpStatus> updateUserLastActivityTime(@RequestParam(name = "userId") String userId, @RequestParam(name = "lastActivityTime", required = false) Date lastActivityTime) {
        return ResponseEntity.ok(userService.updateUserLastActivityTime(userId, lastActivityTime));
    }

    @Operation(summary = "Search users by partial username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found some users.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserSearchResponse.class))}),
            @ApiResponse(responseCode = "404", description = "No user found with given criteria", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/search")
    public ResponseEntity<Page<UserSearchResponse>> searchUser(@RequestParam(name = "username", defaultValue = "") String username,
                                                               @RequestParam(name = "tags", defaultValue = "") String tags,
                                                               @RequestParam(name = "city", defaultValue = "") String city,
                                                               @RequestParam(name = "state", defaultValue = "") String state,
                                                               @RequestParam(value = "page", defaultValue = "0") int page,
                                                               @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(userService.searchUser(username.toLowerCase(), tags, city, state, page, size));
    }

    @Operation(summary = "Create/update user's signup details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opeartion successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignupDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/signupDetails")
    public ResponseEntity<UserSignupDetails> createUserSignupDetails(@RequestBody UserSignupDetails userSignupDetails) {
        return ResponseEntity.ok(userSignupDetailsService.create(userSignupDetails));
    }

    @Operation(summary = "Get User's Signup details by its email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the details.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignupDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/signupDetails")
    public ResponseEntity<List<UserSignupDetails>> getDetailsByEmail(@RequestParam(name = "userEmail") String userEmail) throws Exception {
        return ResponseEntity.ok(userSignupDetailsService.findByUserEmail(userEmail));
    }

    @Operation(summary = "Get list of email addresses by userType")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found some emails.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserEmail.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/emailListByUserType")
    public ResponseEntity<Page<UserEmail>> getEmailListByUserType(@RequestParam(name = "userType", defaultValue = "") String userType,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(userService.getEmailListByUserType(userType, page, size));
    }

    @Operation(summary = "Get map of preferred CPType and their counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/cptypescount")
    public ResponseEntity<Map<String, Integer>> getPreferredCPTypeCounts() throws Exception {
        return ResponseEntity.ok(userService.getPreferredCPTypeCounts());
    }

    @Operation(summary = "Add user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))})})
    @PostMapping("/addUser")
    public Map<String, String> addUser(@RequestParam String email,
                                       @RequestParam Long organizationId,
                                       @RequestParam OrgUserRole role) {
        return userService.addUSer(email, organizationId, role);
    }

    @Operation(summary = "Add user with multiple roles (V1)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))})
    })
    @PostMapping("/v1/addUser")
    public Map<String, String> addUserV1(@RequestParam String name,
                                         @RequestParam String email,
                                         @RequestParam List<OrgUserRole> roles) {
        return userService.addUserV3(name,email, roles);
    }

    @Operation(summary = "Save slack channel details")
    @PostMapping(value = "/slack/channel")
    public ResponseEntity<SlackIntegration> saveSlackIntegration(@RequestParam String userId, @RequestParam String channelId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.saveSlackIntegration(userId, channelId));
    }

    @Operation(summary = "Save slack token")
    @PostMapping(value = "/slack/token")
    public ResponseEntity<IntegrationDetails> saveSlackToken(@RequestBody IntegrationDetails integrationDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.saveSlackToken(integrationDetails));
    }

    @Operation(summary = "Update slack token")
    @PatchMapping(value = "/slack/token")
    public ResponseEntity<IntegrationDetails> updateSlackToken(@RequestBody IntegrationDetails integrationDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateIntegration(integrationDetails));
    }

    @Operation(summary = "Get slack token")
    @GetMapping(value = "/slack/token")
    public IntegrationDetails getSlackToken(@RequestParam String userId) {
        return userService.getSlackToken(userId);
    }

    @PostMapping("/save/continue/free/deal/{userId}")
    public ResponseEntity<DealUserFlagResponse> enableContinueFreeDeal(@PathVariable String userId) {
        userService.enableContinueFreeDeal(userId);
        return ResponseEntity.ok(userService.enableContinueFreeDeal(userId));
    }

    @PostMapping("/save/continue/free/pm/{userId}")
    public ResponseEntity<PmUserFlagResponse> enableContinueFreePartnerMapping(@PathVariable String userId) {
        return ResponseEntity.ok(userService.enableContinueFreePartnerMapping(userId));
    }

    @GetMapping("/check/continue/free/deal/{userId}")
    public ResponseEntity<UserFlagResponse> getContinueFreeDealFlag(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFreeDealFlag(userId));
    }

    @GetMapping("/check/continue/free/pm/{userId}")
    public ResponseEntity<UserFlagResponse> getContinueFreePartnerMappingFlag(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFreePartnerMappingFlag(userId));
    }

    @Operation(summary = "Get all PENDING and EXPIRED invites for organization (V3)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite list fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/team/section/requests")
    public ResponseEntity<SharkdomApiResponse<List<InviteStatusResponseDTO>>> getInvitesByStatus() {
        log.info("[INVITE-LIST] GET /api/v3/invites/status called");
        return ResponseEntity.ok(userService.getPendingAndExpiredInvites());
    }

}
