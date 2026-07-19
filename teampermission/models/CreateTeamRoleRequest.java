package com.sharkdom.teampermission.models;

import lombok.Data;
import java.util.Set;

@Data
public class CreateTeamRoleRequest {
    private String name;
    private String description;
}
