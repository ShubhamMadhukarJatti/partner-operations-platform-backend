package com.sharkdom.gtm.dto;
import com.sharkdom.gtm.common.Status;
import lombok.Data;

@Data
public class UpdateTaskStatusDTO {
    private Status status;
}