package com.sharkdom.pipedrive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePersonRequest {
    private String name;
    private String email;
    private String userId;
}
