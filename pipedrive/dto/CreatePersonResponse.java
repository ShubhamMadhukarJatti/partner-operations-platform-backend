package com.sharkdom.pipedrive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CreatePersonResponse {
    private boolean success;
    private Map<String, Object> data;
}
