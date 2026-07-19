package com.sharkdom.teampermission.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleUserResponse {
    private String userId;
    private String userName;
}