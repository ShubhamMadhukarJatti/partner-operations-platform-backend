package com.sharkdom.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private List<String> partnership_type;
    private List<String> sector;
    private List<String> subsector;
}