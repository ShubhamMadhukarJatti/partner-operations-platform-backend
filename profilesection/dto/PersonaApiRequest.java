package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PersonaApiRequest {
    private List<String> urls;
}