package com.sharkdom.model;

import com.sharkdom.entity.ai.PersonaDetailsEntity;
import com.sharkdom.model.ai.PercentageCategory;
import com.sharkdom.model.ai.PersonaMode;
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
public class PersonaResponse {
    private PersonaStatus personaStatus;
    private PersonaMode mode;
    private Date creationTimestamp;
    private Page<PersonaDetailsEntity> personaDetails;
    private String topIndustry;
    private Double topIndustryPercentage;
    private String topMarketSegment;
    private Double topMarketSegmentPercentage;
    private Map<String, List<PercentageCategory>> category;
}
