package com.sharkdom.teampermission.controller;

import com.sharkdom.teampermission.entity.TeamRole;
import com.sharkdom.teampermission.models.*;
import com.sharkdom.teampermission.service.OrgUserRoleService;
import com.sharkdom.teampermission.service.SharkdomRoles;
import com.sharkdom.teampermission.service.SharkdomRolesService;
import com.sharkdom.teampermission.service.TeamRoleService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/org/team/section")
@RequiredArgsConstructor
@Tag(name = "Organization Roles and Permission")
public class TeamSectionRolesAndPermissionRestController {

    private final OrgUserRoleService roleService;
    private final TeamRoleService teamRoleService;
    private final SharkdomRolesService sharkdomRolesService;

    @PostMapping("/create")
    public ResponseEntity<SharkdomApiResponse<TeamRoleResponse>> createTeamRole(
            @RequestBody CreateTeamRoleRequest request
    ) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Create role request received for orgId={}, roleName={}",
                orgId, request.getName());
        TeamRoleResponse createdRole =
                teamRoleService.createTeamRole(orgId, request);
        log.info("Role created successfully for orgId={}, roleId={}",
                orgId, createdRole.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SharkdomApiResponse<>(
                        true,
                        "Role created successfully",
                        createdRole
                ));
    }

    @PatchMapping("/roles/{roleId}/permissions")
    public ResponseEntity<SharkdomApiResponse<String>> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsRequest request
    ) {

        Long orgId = Util.getOrgIdFromToken();

        teamRoleService.updateRolePermissions(orgId, roleId, request);

        SharkdomApiResponse<String> response =
                new SharkdomApiResponse<>(
                        true,
                        "Role permissions updated successfully",
                        "UPDATED"
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get organization roles with assigned users",
            description = "Fetches all roles for an organization along with their permission codes and assigned user names"
    )
    @GetMapping("/roles/with/users")
    public ResponseEntity<SharkdomApiResponse<List<TeamRoleWithUsersResponse>>> getRolesWithUsers() {
        var orgId = Util.getOrgIdFromToken();
//        var orgId = 1433l;
        List<TeamRoleWithUsersResponse> roles =
                teamRoleService.getRolesWithUsersByOrg(orgId);
        SharkdomApiResponse<List<TeamRoleWithUsersResponse>> response =
                new SharkdomApiResponse<>(
                        true,
                        "Roles fetched successfully",
                        roles
                );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sharkdom/roles")
    public ResponseEntity<SharkdomApiResponse<SharkdomRoles>> createSharkdomRole(
            @RequestBody CreateRoleRequest request) {

        log.info("API request received to create role. roleName={}", request.getRoleName());

        SharkdomApiResponse<SharkdomRoles> response =
                sharkdomRolesService.createRole(
                        request.getRoleName(),
                        request.getDescription()
                );

        log.info("API response sent for create role. roleName={}", request.getRoleName());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/sharkdom/roles")
    public ResponseEntity<SharkdomApiResponse<List<SharkdomRoles>>> getAllSharkdomRoles() {

        log.info("API request received to fetch all roles");

        SharkdomApiResponse<List<SharkdomRoles>> response =
                sharkdomRolesService.getAllRoles();

        log.info("API response sent for fetch roles");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/org/roles")
    public ResponseEntity<SharkdomApiResponse<List<TeamRoleListResponse>>> getRolesByOrg(
           ) {
        var orgId = Util.getOrgIdFromToken();
        log.info("API request received to fetch roles for orgId={}", orgId);

        SharkdomApiResponse<List<TeamRoleListResponse>> response =
                teamRoleService.getRolesByOrg(orgId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/roles/assign")
    public ResponseEntity<SharkdomApiResponse<String>> assignRoles(
            @RequestBody UserRoleUpdateRequest request
    ) {

        log.info("Assign roles request received. userId={}, roles={}",
                request.getUserId(), request.getRoleNames());

        teamRoleService.assignRoles(
                request.getUserId(),
                request.getRoleNames()
        );

        SharkdomApiResponse<String> response =
                new SharkdomApiResponse<>(
                        true,
                        "Roles assigned successfully",
                        "ASSIGNED"
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/roles/remove")
    public ResponseEntity<SharkdomApiResponse<String>> removeRoles(
            @RequestBody UserRoleUpdateRequest request
    ) {

        log.info("Remove roles request received. userId={}, roles={}",
                request.getUserId(), request.getRoleNames());

       teamRoleService.removeRoles(
                request.getUserId(),
                request.getRoleNames()
        );

        SharkdomApiResponse<String> response =
                new SharkdomApiResponse<>(
                        true,
                        "Roles removed successfully",
                        "REMOVED"
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<SharkdomApiResponse<List<TeamUserRoleResponse>>> getRolesByUser(
            @PathVariable String userId
    ) {

        log.info("Fetch roles request received for userId={}", userId);

        List<TeamUserRoleResponse> roles =
                teamRoleService.getAllRolesByUserId(userId);

        SharkdomApiResponse<List<TeamUserRoleResponse>> response =
                new SharkdomApiResponse<>(
                        true,
                        "Roles fetched successfully",
                        roles
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/roles/users")
    public ResponseEntity<SharkdomApiResponse<List<RoleUserResponse>>>
    getUsersByOrgAndRole(
           @RequestBody GetUserRolesRequest getUserRolesRequest
    ) {
        Long orgId=Util.getOrgIdFromToken();
        List<RoleUserResponse> users =
                teamRoleService.getUsersByOrgAndRoleName(orgId, getUserRolesRequest.getRoleName());

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Users fetched successfully",
                        users
                )
        );
    }
}
