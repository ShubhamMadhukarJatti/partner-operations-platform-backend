package com.sharkdom.mapper;

import com.sharkdom.entity.organizationcollaboration.OrganizationMessages;
import com.sharkdom.model.organizatiocollaboration.OrganizationMessagesResponse;

public class OrganizationMessagesMapper {
    private OrganizationMessagesMapper() {
    }

    public static OrganizationMessagesResponse toOrganizationMessagesResponse(OrganizationMessages organizationMessages, String query) {
        OrganizationMessagesResponse organizationMessagesResponse = new OrganizationMessagesResponse();
        organizationMessagesResponse.setChatRoomId(organizationMessages.getChatRoomId());
        organizationMessagesResponse.setQuery(query);
        organizationMessagesResponse.setLinkerId(organizationMessages.getLinkerId());
        organizationMessagesResponse.setLinkerType(organizationMessages.getLinkerType());
        organizationMessagesResponse.setFlag(organizationMessages.getFlag());
        organizationMessagesResponse.setChannelFlag(organizationMessages.getChannelFlag());
        organizationMessagesResponse.setRead(organizationMessages.isRead());
        organizationMessagesResponse.setReadAt(organizationMessages.getReadAt());
        organizationMessagesResponse.setSenderId(organizationMessages.getSenderId());
        organizationMessagesResponse.setReceiverId(organizationMessages.getReceiverId());
        organizationMessagesResponse.setId(organizationMessages.getId());
        organizationMessagesResponse.setCreationTimestamp(organizationMessages.getCreationTimestamp());
        organizationMessagesResponse.setLastUpdatedTimestamp(organizationMessages.getLastUpdatedTimestamp());
        return organizationMessagesResponse;
    }
}
