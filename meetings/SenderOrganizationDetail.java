package com.sharkdom.model.meetings;

import lombok.Builder;

@Builder
public record SenderOrganizationDetail(Long organizationId, String logoUrl) {

}
