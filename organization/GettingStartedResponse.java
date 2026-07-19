package com.sharkdom.model.organization;

import com.sharkdom.entity.organization.GettingStartedEntity;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class GettingStartedResponse {
    private ProgressResponse profileSetup;
    private ProgressResponse inviteMemberSetup;
    private ProgressResponse customerPersonaSetup;
    private ProgressResponse proposalSetup;
    private ProgressResponse preferredMeetSetup;
    private ProgressResponse addPartnersSetup;
    private List<PendingCollaboration> pendingCollaborations;
    private boolean slackConnected;
    private boolean proposalSent;
    private boolean integrationProgramCreated;
    private GettingStartedEntity.NotFiled inHouseTeam;
    private GettingStartedEntity.NotFiled apiProgram;
    private String brandingPage;
    private String activePartnerProgram;
    private String currentPartnerCount;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class PendingCollaboration {
        private String organizationName;
        private Long organizationCollaborationId;
        private String logoUrl;
    }

}


