package com.sharkdom.mypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerStatusResponseDTO {

    private boolean mouUploaded;
    private boolean signedByY;
    private boolean partnershipEnabled;
    private boolean trainingCredentialSent;
}