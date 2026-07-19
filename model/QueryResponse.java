package com.sharkdom.agenticai.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryResponse {

    private List<String> partnership_type;
    private List<String> sector;
    private List<String> subsector;

}
