package com.sharkdom.entity.user;

public enum InviteTeamMemberStatus {
    PENDING,        // Invite sent but not yet accepted or rejected
    ACCEPTED,       // Invite accepted by the user
    REJECTED,       // Invite declined by the user
    EXPIRED,        // Invite link expired (e.g., after a certain duration)
    CANCELLED,      // Invite cancelled by the sender (organization/admin)
    FAILED,          // Invite failed to send due to some error (e.g., email not delivered)
    ONBOARDED,
    ACTIVE
}
