package com.sharkdom.teampermission.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamUserRoleResponse {

    private Long id;
    private String name;
    private Long organizationId; // null = global
}
