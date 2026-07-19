package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEmailReqModel {

    String templateCode;
    List<String> userIds;
    String s3AttachmentNames;
    String emailVerifyLink;
    String organization1Name;
    String organization2Name;
    String organization1Desc;
    String organization2Desc;
    String organization1Logo;
    String organization2Logo;
    String followingDate;
    String followerOrganizationName;
    private String username;
    String followingOrganizationName;
    private String organizationCode;
    private String emailUnsubscribeLink;
}
