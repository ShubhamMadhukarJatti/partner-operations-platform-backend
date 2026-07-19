package com.sharkdom.model.ai;

import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import lombok.Data;
import java.util.Set;
import java.util.Map;

@Data
public class PartnerDataPermission {
    private Long organizationId ;
    private CollaborationCategory collaborationCategory;
    private Map<RecordType, PermissionDetails> permissions;
    
    public enum AccessType {
        FULL_ACCESS,
        ONLY_COUNT,
        HIDDEN,
        PARTIAL
    }
    
    public enum AvailableField {
        name,
        companyName,
        contactEmail,
        domain,
        dealStage,
        creationDate,
        closeDate,
        subscribed,
        ticketSize
    }

    @Data
    public static class PermissionDetails {
        private AccessType accessType;
        private Set<String> sharedFields;
    }
} 