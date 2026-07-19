package com.sharkdom.teampermission.models;


import lombok.Data;

@Data
public class CreateRoleRequest {
    private String roleName;
    private String description;
}