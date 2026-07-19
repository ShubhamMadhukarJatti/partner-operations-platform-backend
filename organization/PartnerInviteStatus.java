package com.sharkdom.model.organization;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartnerInviteStatus {
    private boolean isEmailOpened;
    private boolean isEmailClicked;
}
