package com.sharkdom.model.persona;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PersonaMatchDto {

    private Boolean currentOrgPersonaStatus;
    private Boolean targetOrgPersonaStatus;
    private Map<String, Object> personaMatch;

}
