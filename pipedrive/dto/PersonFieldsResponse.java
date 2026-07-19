package com.sharkdom.pipedrive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class PersonFieldsResponse {
    private boolean success;
    private List<Map<String, Object>> data;
}
