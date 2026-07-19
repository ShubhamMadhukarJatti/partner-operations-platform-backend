package com.sharkdom.dto;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;


@Data
public class InviteStatusResponseDTO {


    private String userId;
    private String name;
    private String email;
    private List<OrgUserRole> roles;
    private InviteTeamMemberStatus status;
    private LocalDateTime requestSentAt;
    private LocalDateTime expiresAt;
    private boolean seatConsumed;
}