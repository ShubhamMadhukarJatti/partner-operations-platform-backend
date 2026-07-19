package com.sharkdom.model.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;
@Builder
@AllArgsConstructor
public class ModeSaveResponse {
    Long organizationId;
    String mode;
    Date clickedOn;
    boolean created;
    Date createdOn;
}
