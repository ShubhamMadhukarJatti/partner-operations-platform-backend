package com.sharkdom.model.ppi;

import lombok.Data;

import java.util.Date;

@Data
public class LogResponse {
    private String updatedFormStatus;
    private String modifiedBy;
    private Date modifiedDate;
}
