package com.sharkdom.model.mou;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SignatureRequestModel {
    private String documentId;
    private String redirectUrl;
    private List<SignersModel> signers;
}
