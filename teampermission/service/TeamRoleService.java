package com.sharkdom.teampermission.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.user.OrganizationUserRoleMapping;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.user.OrganizationUserRoleMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.teampermission.entity.*;
import com.sharkdom.teampermission.models.*;
import com.sharkdom.teampermission.repository.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamRoleService {

    private final TeamRoleRepository roleRepository;
    private final TeamSectionPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final OrganizationUserRoleMappingRepository organizationUserRoleMappingRepository;
    private final SharkdomRolesRepository sharkdomRolesRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamUserRolesAssignmentRepository teamUserRolesAssignmentRepository;
    private final TeamUserRolesRepository teamUserRolesRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public TeamRole createRole(Long orgId, CreateTeamRoleRequest request) {

        // Optional: Prevent duplicate role per org
        roleRepository.findByOrgIdAndName(orgId, request.getName())
                .ifPresent(r -> {
                    throw new IllegalStateException(
                            "Role already exists for this org: " + request.getName()
                    );
                });

        TeamRole role = TeamRole.builder()
                .name(request.getName())
                .description(request.getDescription())
                .orgId(orgId)
                .build();

        return roleRepository.save(role);
    }

    private List<TeamRoleWithUsersResponse> buildEmptyRoleResponse(List<TeamRole> roles) {

        return roles.stream()
                .map(role -> {
                    TeamRoleWithUsersResponse dto = new TeamRoleWithUsersResponse();
                    dto.setRoleId(role.getId());
                    dto.setRoleName(role.getName());
                    dto.setDescription(role.getDescription());
                    dto.setPermissionCodes(role.getPermissionCodes());
                    dto.setUserNames(Collections.emptyList());
                    return dto;
                })
                .toList();
    }

    @Transactional
    public TeamRoleResponse createTeamRole(Long orgId, CreateTeamRoleRequest request) {

        log.info("Attempting to create role for orgId={}", orgId);

        if (request == null) {
            log.error("CreateTeamRoleRequest is null for orgId={}", orgId);
            throw new ServiceException(ErrorMessages.SH189);
        }

        String roleName = request.getName();

        if (roleName == null || roleName.trim().isEmpty()) {
            log.error("Role name is blank for orgId={}", orgId);
            throw new ServiceException(ErrorMessages.SH189);
        }

        roleName = roleName.trim();

        log.debug("Creating role with name={} for orgId={}", roleName, orgId);

        // Case-insensitive uniqueness check
        String finalRoleName = roleName;
        roleRepository.findByOrgIdAndNameIgnoreCase(orgId, roleName)
                .ifPresent(existingRole -> {
                    log.error("Duplicate role detected. orgId={}, roleName={}", orgId, finalRoleName);
                    throw new ServiceException(ErrorMessages.SH161, finalRoleName);
                });

        try {

            TeamRole role = TeamRole.builder()
                    .name(roleName)
                    .description(request.getDescription())
                    .orgId(orgId)
                    .build();

            TeamRole saved = roleRepository.save(role);

            log.info("Role saved successfully. roleId={}, orgId={}",
                    saved.getId(), orgId);

            return TeamRoleResponse.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .description(saved.getDescription())
                    .build();

        } catch (DataIntegrityViolationException ex) {

            log.error("Database unique constraint violation for orgId={}, roleName={}",
                    orgId, roleName, ex);

            throw new ServiceException(ErrorMessages.SH161, roleName);

        } catch (Exception ex) {

            log.error("Unexpected error while creating role for orgId={}", orgId, ex);

            throw new RuntimeException("Failed to create role due to an unexpected error");
        }
    }

    @Transactional
    public void updateRolePermissions(Long orgId,
                                      Long roleId,
                                      UpdateRolePermissionsRequest request) {

        // Role must exist
        TeamRole role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ServiceException(ErrorMessages.SH186, roleId)
                );

        // Role must belong to org
        if (!role.getOrgId().equals(orgId)) {
            throw new ServiceException(ErrorMessages.SH187, orgId);
        }

        // Validate permissions
        List<TeamSectionPermission> permissions = permissionRepository.findByCodeInIgnoreCase(request.getPermissionCodes());

        // Replace permissions
        role.setPermissionCodes(new HashSet<>(request.getPermissionCodes()));

        roleRepository.save(role);
    }

    private void syncMasterRolesToOrg(Long orgId) {

        log.info("Syncing master roles into orgId={}", orgId);

        List<SharkdomRoles> masterRoles = sharkdomRolesRepository.findAll();

        if (masterRoles.isEmpty()) {
            log.warn("No master roles found in t_sharkdom_roles");
            return;
        }

        for (SharkdomRoles masterRole : masterRoles) {

            boolean exists = roleRepository.existsByOrgIdAndName(
                    orgId,
                    masterRole.getRoleName()
            );

            if (!exists) {

                TeamRole newRole = TeamRole.builder()
                        .name(masterRole.getRoleName())
                        .description(masterRole.getDescription())
                        .orgId(orgId)
                        .permissionCodes(new HashSet<>())
                        .build();

                roleRepository.save(newRole);

                log.info("Added missing role '{}' to orgId={}",
                        masterRole.getRoleName(),
                        orgId);
            }
        }
    }

    @Transactional
    public List<TeamRoleWithUsersResponse> getRolesWithUsersByOrg(Long orgId) {

        log.info("Fetching roles with users for orgId={}", orgId);

        // Sync master roles into org-specific roles
        syncMasterRolesToOrg(orgId);

        // Fetch all roles for org
        List<TeamRole> roles = roleRepository.findAllByOrgId(orgId);
        if (roles.isEmpty()) {
            log.info("No roles found for orgId={}", orgId);
            return Collections.emptyList();
        }

        // Fetch role mappings ONLY for this org
        List<OrganizationUserRoleMapping> mappings =
                organizationUserRoleMappingRepository.findAll();

        Map<OrgUserRole, List<String>> roleToUserIds =
                mappings == null ? Collections.emptyMap() :
                        mappings.stream()
                                .filter(m -> m.getUserId() != null && m.getRole() != null)
                                .collect(Collectors.groupingBy(
                                        OrganizationUserRoleMapping::getRole,
                                        Collectors.mapping(
                                                OrganizationUserRoleMapping::getUserId,
                                                Collectors.toList()
                                        )
                                ));

        List<String> allUserIds =
                roleToUserIds.values()
                        .stream()
                        .flatMap(List::stream)
                        .distinct()
                        .toList();

        Map<String, User> userMap =
                allUserIds.isEmpty()
                        ? Collections.emptyMap()
                        : userRepository.findByUserIdIn(allUserIds)
                        .stream()
                        .filter(u -> u.getUserId() != null)
                        .collect(Collectors.toMap(
                                User::getUserId,
                                Function.identity()
                        ));

        List<TeamRoleWithUsersResponse> responseList = new ArrayList<>();

        for (TeamRole role : roles) {

            TeamRoleWithUsersResponse dto = new TeamRoleWithUsersResponse();
            dto.setRoleId(role.getId());
            dto.setRoleName(role.getName());
            dto.setDescription(role.getDescription());
            dto.setPermissionCodes(role.getPermissionCodes());

            List<String> userNames = new ArrayList<>();

            try {
                OrgUserRole enumRole = OrgUserRole.valueOf(role.getName());

                userNames = roleToUserIds
                        .getOrDefault(enumRole, Collections.emptyList())
                        .stream()
                        .map(userMap::get)
                        .filter(Objects::nonNull)
                        .map(User::getName)
                        .filter(Objects::nonNull)
                        .toList();

            } catch (IllegalArgumentException e) {
                log.warn("Role name '{}' does not match OrgUserRole enum", role.getName());
            }
            var roleUserResponses =
                    Optional.ofNullable(getUsersByOrgAndRoleName(orgId, role.getName()))
                            .orElse(Collections.emptyList());

            List<String> userNameList = roleUserResponses.stream()
                    .map(RoleUserResponse::getUserName)
                    .toList();

            dto.setUserNames(userNameList);
            responseList.add(dto);
        }

        return responseList;
    }

    @Transactional
    public void assignRoles(String userId, List<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            throw new ServiceException(ErrorMessages.SH195);
        }

        Long organizationId = Util.getOrgIdFromToken(); // Replace with Util.getOrgIdFromToken();
//        Long organizationId = 1433l; // Replace with Util.getOrgIdFromToken();

        if (organizationId == null) {
            assignGlobalRolesUpsert(userId, roleNames);
        } else {
            assignOrgRolesUpsert(userId, organizationId, roleNames);
        }
    }

    private void assignOrgRolesUpsert(String userId,
                                      Long organizationId,
                                      List<String> roleNames) {

        // 🔹 1. Ensure Team Member Exists
        TeamMembers member = teamMemberRepository
                .findByUserIdAndOrganizationId(userId, organizationId)
                .orElseGet(() -> {
                    TeamMembers newMember = new TeamMembers();
                    newMember.setUserId(userId);
                    newMember.setOrganizationId(organizationId);
                    return teamMemberRepository.saveAndFlush(newMember);
                });

        // 🔹 2. Fetch existing roles for this organization
        List<TeamUserRoles> existingRoles =
                teamUserRolesRepository
                        .findByNameInAndOrganizationId(roleNames, organizationId);

        Map<String, TeamUserRoles> roleMap = existingRoles.stream()
                .collect(Collectors.toMap(TeamUserRoles::getName, r -> r));

        // 🔹 3. Create missing roles
        for (String roleName : roleNames) {

            if (!roleMap.containsKey(roleName)) {

                TeamUserRoles newRole = new TeamUserRoles();
                newRole.setName(roleName);
                newRole.setOrganizationId(organizationId);

                TeamUserRoles savedRole =
                        teamUserRolesRepository.saveAndFlush(newRole);

                roleMap.put(roleName, savedRole);
            }
        }

        // 🔹 4. Assign roles using Assignment table (NO duplicates)
        for (TeamUserRoles role : roleMap.values()) {

            boolean alreadyAssigned =
                    teamUserRolesAssignmentRepository
                            .existsByUserIdAndRole_Id(userId, role.getId());

            if (!alreadyAssigned) {

                TeamUserRolesAssignment assignment =
                        new TeamUserRolesAssignment();

                assignment.setUserId(userId);
                assignment.setRole(role);
                assignment.setOrganizationId(organizationId);

                teamUserRolesAssignmentRepository.save(assignment);
            }
        }
    }

    private void assignGlobalRolesUpsert(String userId,
                                         List<String> roleNames) {

        List<TeamUserRoles> existingRoles =
                teamUserRolesRepository.findByNameInAndOrganizationIdIsNull(roleNames);

        Map<String, TeamUserRoles> roleMap = existingRoles.stream()
                .collect(Collectors.toMap(TeamUserRoles::getName, r -> r));

        // 🔹 Create missing roles
        for (String roleName : roleNames) {

            if (!roleMap.containsKey(roleName)) {

                TeamUserRoles newRole = new TeamUserRoles();
                newRole.setName(roleName);
                newRole.setOrganizationId(null);

                TeamUserRoles savedRole = teamUserRolesRepository.saveAndFlush(newRole);
                roleMap.put(roleName, savedRole);
            }
        }

        // 🔹 Assign roles (no duplicates)
        for (TeamUserRoles role : roleMap.values()) {

            boolean alreadyAssigned =
                    teamUserRolesAssignmentRepository
                            .existsByUserIdAndRoleId(userId, role.getId());

            if (!alreadyAssigned) {
                TeamUserRolesAssignment assignment = new TeamUserRolesAssignment();
                assignment.setUserId(userId);
                assignment.setRole(role);
                teamUserRolesAssignmentRepository.save(assignment);
            }
        }
    }



    @Transactional
    public void removeRoles(String userId, List<String> roleNames) {
        Long organizationId=Util.getOrgIdFromToken();
        if (roleNames == null || roleNames.isEmpty()) {
            throw new ServiceException(ErrorMessages.SH195);
        }

        if (organizationId == null) {
            removeGlobalRoles(userId, roleNames);
        } else {
            removeOrgRoles(userId, organizationId, roleNames);
        }
    }

    private void removeGlobalRoles(String userId, List<String> roleNames) {

        List<TeamUserRoles> roles =
                teamUserRolesRepository.findByNameInAndOrganizationIdIsNull(roleNames);

        if (roles.size() != roleNames.size()) {
            throw new ServiceException(ErrorMessages.SH190, roleNames);
        }

        for (TeamUserRoles role : roles) {

            TeamUserRolesAssignment assignment =
                    teamUserRolesAssignmentRepository
                            .findByUserIdAndRoleId(userId, role.getId())
                            .orElseThrow(() ->
                                    new ServiceException(ErrorMessages.SH193)
                            );

            teamUserRolesAssignmentRepository.delete(assignment);
        }
    }

    private void removeOrgRoles(String userId,
                                Long organizationId,
                                List<String> roleNames) {

        //  Get Team Member
        TeamMembers member = teamMemberRepository
                .findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() ->
                        new ServiceException(
                                ErrorMessages.SH194,
                                userId,
                                organizationId
                        )
                );

        //  Fetch Roles for Org
        List<TeamUserRoles> roles =
                teamUserRolesRepository
                        .findByNameInAndOrganizationId(roleNames, organizationId);

        if (roles.size() != roleNames.size()) {
            throw new ServiceException(
                    ErrorMessages.SH191,
                    roleNames,
                    organizationId
            );
        }

        // Validate & Remove
        for (TeamUserRoles role : roles) {

            member.getRoles()
                    .removeIf(r -> r.getId().equals(role.getId()));
        }

        // Save updated member
        teamMemberRepository.save(member);
    }

    @Transactional
    public List<TeamUserRoleResponse> getAllRolesByUserId(String userId) {

        List<TeamUserRoleResponse> response = new ArrayList<>();

        List<TeamUserRolesAssignment> globalAssignments =
                teamUserRolesAssignmentRepository.findByUserId(userId);

        for (TeamUserRolesAssignment assignment : globalAssignments) {

            TeamUserRoles role = assignment.getRole();

            response.add(new TeamUserRoleResponse(
                    role.getId(),
                    role.getName(),
                    null
            ));
        }

        List<TeamMembers> members =
                teamMemberRepository.findByUserId(userId);

        for (TeamMembers member : members) {

            Long organizationId = member.getOrganizationId();

            for (TeamUserRoles role : member.getRoles()) {

                response.add(new TeamUserRoleResponse(
                        role.getId(),
                        role.getName(),
                        organizationId
                ));
            }
        }

        response.add(new TeamUserRoleResponse(
                null,
                "User",
                null
        ));

        if (response.isEmpty()) {
            throw new ServiceException(
                    ErrorMessages.SH196,
                    userId
            );
        }

        return response;
    }

    @Transactional
    public List<RoleUserResponse> getUsersByOrgAndRoleNameV3(Long orgId, String roleName) {

        TeamUserRoles role = teamUserRolesRepository
                .findByOrganizationIdAndName(orgId, roleName)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH197));

        List<TeamUserRolesAssignment> assignments =
                teamUserRolesAssignmentRepository
                        .findByRoleIdAndOrganizationId(role.getId(), orgId);

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> userIds = assignments.stream()
                .map(TeamUserRolesAssignment::getUserId)
                .toList();

        List<User> users = userRepository.findByUserIdIn(userIds);

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        return userIds.stream()
                .map(userId -> {
                    User user = userMap.get(userId);
                    return new RoleUserResponse(
                            userId,
                            user != null ? user.getName() : null
                    );
                })
                .toList();
    }

    @Transactional
    public List<RoleUserResponse> getUsersByOrgAndRoleNameV2(Long orgId, String roleName) {

        // Step 1: Fetch role by org + roleName
        TeamUserRoles role = teamUserRolesRepository
                .findByOrganizationIdAndName(orgId, roleName)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH197));

        // Step 2: Fetch assignments for this role
        List<TeamUserRolesAssignment> assignments =
                teamUserRolesAssignmentRepository.findByRole(role);

        if (assignments.isEmpty()) {
            throw new ServiceException(ErrorMessages.SH197);
        }

        // Step 3: Collect userIds
        List<String> userIds = assignments.stream()
                .map(TeamUserRolesAssignment::getUserId)
                .toList();

        // Step 4: Fetch users in single query
        List<User> users = userRepository.findByUserIdIn(userIds);

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // Step 5: Build response
        return userIds.stream()
                .map(userId -> {
                    User user = userMap.get(userId);
                    return new RoleUserResponse(
                            userId,
                            user != null ? user.getName() : null
                    );
                })
                .toList();
    }

    @Transactional
    public SharkdomApiResponse<List<TeamRoleListResponse>> getRolesByOrg(Long orgId) {

        log.info("Fetching roles for orgId={}", orgId);

        // 🔹 Step 1: Sync master roles first
        syncMasterRolesToOrg(orgId);

        // 🔹 Step 2: Fetch updated roles
        List<TeamRole> roles = roleRepository.findAllByOrgId(orgId);

        List<TeamRoleListResponse> response = roles.stream()
                .map(role -> new TeamRoleListResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription()
                ))
                .toList();

        log.info("Successfully fetched {} roles for orgId={}", response.size(), orgId);

        return new SharkdomApiResponse<>(
                true,
                "Roles fetched successfully",
                response
        );
    }

    @Transactional
    public List<RoleUserResponse> getUsersByOrgAndRoleName(Long orgId, String roleName) {

        Optional<TeamUserRoles> optionalRole =
                teamUserRolesRepository.findByOrganizationIdAndName(orgId, roleName);

        if (optionalRole.isEmpty()) {
            log.info("Role '{}' not found in TeamUserRoles for orgId={}", roleName, orgId);
            return Collections.emptyList();
        }

        TeamUserRoles role = optionalRole.get();

        List<TeamUserRolesAssignment> assignments =
                teamUserRolesAssignmentRepository.findByRole(role);

        if (assignments.isEmpty()) {
            log.info("No users assigned to role '{}' for orgId={}", roleName, orgId);
            return Collections.emptyList();
        }

        List<String> userIds = assignments.stream()
                .map(TeamUserRolesAssignment::getUserId)
                .toList();

        List<User> users = userRepository.findByUserIdIn(userIds);

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        return userIds.stream()
                .map(userId -> {
                    User user = userMap.get(userId);
                    return new RoleUserResponse(
                            userId,
                            user != null ? user.getName() : null
                    );
                })
                .toList();
    }

}
