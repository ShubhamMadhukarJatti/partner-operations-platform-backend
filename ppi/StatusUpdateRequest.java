package com.sharkdom.model.ppi;

import com.sharkdom.constants.ppi.FormStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    private Long responseId;
    private String userId;
    private FormStatus status;


}
