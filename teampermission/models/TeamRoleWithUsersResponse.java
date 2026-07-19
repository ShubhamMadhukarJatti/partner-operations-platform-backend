package com.sharkdom.teampermission.models;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class TeamRoleWithUsersResponse {

    private Long roleId;
    private String roleName;
    private String description;
    private Set<String> permissionCodes;
    private List<String> userNames; // ONLY NAMES
}
