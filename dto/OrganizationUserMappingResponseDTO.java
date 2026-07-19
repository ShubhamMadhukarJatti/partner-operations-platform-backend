package com.sharkdom.dto;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserMappingResponseDTO {

    private String name;
    private String email;
    private List<OrgUserRole> roles;
    private Date requestSendDate;
    private String userId;
    private Long organizationId;
    private InviteTeamMemberStatus inviteTeamMemberStatus;
}
