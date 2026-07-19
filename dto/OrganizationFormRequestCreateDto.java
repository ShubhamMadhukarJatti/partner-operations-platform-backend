package com.sharkdom.dto;


import com.sharkdom.entity.ppi.FormStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationFormRequestCreateDto {
    private String email;
    private String formId;
    private FormStatus status;
    private boolean isExternalUser;
}
