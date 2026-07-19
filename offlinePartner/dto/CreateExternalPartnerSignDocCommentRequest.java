package com.sharkdom.offlinePartner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExternalPartnerSignDocCommentRequest {

    private String externalPartnerCode;

    private String commentText;
}