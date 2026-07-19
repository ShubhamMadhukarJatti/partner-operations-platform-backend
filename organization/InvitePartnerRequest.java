package com.sharkdom.model.organization;

import java.util.List;

public record InvitePartnerRequest(Long organizationId, String message, List<BulkInvite> bulkInvite) {

}
