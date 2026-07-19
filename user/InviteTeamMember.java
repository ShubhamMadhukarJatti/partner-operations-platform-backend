package com.sharkdom.entity.user;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_invite_team_member")
@Data
public class InviteTeamMember extends BaseEntity {


    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "user_id")
    private String userId;


    @Column(name = "org_id", nullable = false)
    private Long orgId;


    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false)
    private InviteTeamMemberStatus inviteTeamMemberStatus;


    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "invite_roles", joinColumns = @JoinColumn(name = "invite_id"))
    @Column(name = "role")
    private List<OrgUserRole> roles;


    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;


    @Column(name = "seat_consumed", nullable = false)
    private boolean seatConsumed;
}