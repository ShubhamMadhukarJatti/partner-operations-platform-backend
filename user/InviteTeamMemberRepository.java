package com.sharkdom.repository.user;

import com.sharkdom.entity.user.InviteTeamMember;
import com.sharkdom.entity.user.InviteTeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InviteTeamMemberRepository extends JpaRepository<InviteTeamMember,Long> {
    Optional<InviteTeamMember> findByUserId(String userId);

    List<InviteTeamMember> findByOrgId(Long orgIdFromToken);

    Optional<InviteTeamMember> findByEmail(String email);

    Optional<InviteTeamMember> findByEmailAndOrgId(String email, Long orgId);

    List<InviteTeamMember> findByInviteTeamMemberStatusAndExpiresAtBefore(
            InviteTeamMemberStatus status,
            LocalDateTime time
    );

    List<InviteTeamMember> findByOrgIdAndInviteTeamMemberStatusIn(
            Long orgId,
            List<InviteTeamMemberStatus> statuses
    );

    List<InviteTeamMember> findAllByOrgIdAndInviteTeamMemberStatus(
            Long orgId,
            InviteTeamMemberStatus status
    );

}
