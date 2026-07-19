package com.sharkdom.partnerattribution.dto;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentsResponse {
    private List<String> departments;
    private String description;
}
