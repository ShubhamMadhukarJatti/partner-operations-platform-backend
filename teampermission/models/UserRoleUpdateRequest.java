package com.sharkdom.teampermission.models;

import lombok.Data;
import java.util.List;

@Data
public class UserRoleUpdateRequest {

    private String userId;
    private List<String> roleNames;
}
