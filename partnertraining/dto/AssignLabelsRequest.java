package com.sharkdom.partnertraining.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AssignLabelsRequest {

    private Set<Long> labelIds;
}