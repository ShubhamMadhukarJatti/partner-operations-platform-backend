package com.sharkdom.model.ppi;

import com.sharkdom.constants.ppi.FormStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class InternalFormResponse {
    private String applicantName;
    private String email;
    private Date date;
    private FormStatus status;
    private String brand;
    private Long responseId;
    private List<Long> responseIdList;
    private Boolean isExternalSubmission;
}
