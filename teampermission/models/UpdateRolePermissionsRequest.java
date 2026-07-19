package com.sharkdom.teampermission.models;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolePermissionsRequest {

    @NotEmpty(message = "Permission codes cannot be empty")
    private Set<String> permissionCodes;
}
