package com.sharkdom.model.meetings;

import lombok.Builder;

@Builder
public record ReceiverOrganizationDetail(Long organizationId, String logoUrl) {

}
