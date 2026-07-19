package com.sharkdom.model.ai;

import lombok.Data;

import java.util.List;

@Data
public class SharkqQueryRequest {
    int promptId;
    String query;
    List<Long> orgIdList;
}
