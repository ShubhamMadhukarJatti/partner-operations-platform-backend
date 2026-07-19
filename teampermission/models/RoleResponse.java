package com.sharkdom.teampermission.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleResponse {
    private String key;   // ENUM NAME
    private String name;  // Human readable
}
