package com.sharkdom.offlinePartner.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OfflinePartnerInviteResponse {
    private Long id;
    private String email;
    private String name;
    private boolean verified;
    private boolean onboarded;
    private String logoUrl;
    private String code;
    private String remarks;
    private boolean verifyEmailSent;
    private boolean verifyEmailOpened;
    private boolean verifyEmailClicked;
    private boolean inviteEmailOpened;
    private boolean inviteEmailClicked;
    private boolean isMailBoxClaimed;
}
