package com.sharkdom.model.ai;

import com.sharkdom.model.ai.PartnerDataPermission.AccessType;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDataPermissionResponse {
    private Long organizationId;
    private List<PermissionDetails> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDetails {
        private CollaborationCategory collaborationCategory;
        private Map<RecordType, RecordPermission> recordPermissions;
        private Date lastModified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordPermission {
        private PartnerDataPermission.AccessType accessType;
        private Set<String> sharedFields;
    }
} 