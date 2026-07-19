package com.sharkdom.model.organizatiocollaboration;

import java.util.Date;


public interface CollaborationRepositoryResponse {
    Long getId();

    Long getOrganizationId();

    Date getCreationTimestamp();

    String getStatus();

}
