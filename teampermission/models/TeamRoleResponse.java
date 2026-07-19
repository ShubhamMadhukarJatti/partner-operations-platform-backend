package com.sharkdom.teampermission.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TeamRoleResponse {
    private Long id;
    private String name;
    private String description;
}