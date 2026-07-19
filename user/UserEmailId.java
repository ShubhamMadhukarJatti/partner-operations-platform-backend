package com.sharkdom.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEmailId {
    private String email;
    private String userId;
    private String name;
}