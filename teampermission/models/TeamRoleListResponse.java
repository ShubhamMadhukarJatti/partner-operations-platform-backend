package com.sharkdom.teampermission.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamRoleListResponse {

    private Long roleId;
    private String name;
    private String description;
}