package com.sharkdom.offlinePartner.model;

import com.sharkdom.entity.ai.PersonaDetailsEntity;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.PercentageCategory;
import com.sharkdom.offlinePartner.entity.OfflinePersonaDetailsEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class OfflinePersonaResponse {
    private boolean organizationPersonaCompleted;
    private boolean partnerPersonaCompleted;
    private PersonaStatus personaStatus;
    private Date creationTimestamp;
    private Page<PersonaDetailsEntity> organizationPersonaDetails;
    private Page<OfflinePersonaDetailsEntity> partnerPersonaDetails;
    private Map<String, List<PercentageCategory>> organizationCategory;
    private Map<String, List<PercentageCategory>> partnerCategory;
}
