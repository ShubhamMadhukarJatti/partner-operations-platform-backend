package com.sharkdom.dto;

import com.sharkdom.entity.ppi.PartnerPortalBranding;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormQuestionResponse {

    private List<?> questions;
    private PartnerPortalBranding branding;
}

