package com.sharkdom.model.ai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sharkdom.util.StringListToCommaSeparatedDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PersonaModelResponse {

    @JsonDeserialize(using = StringListToCommaSeparatedDeserializer.class)
    private String companySector;
    private String companySize;
    private String isPartnershipProgram;

    @JsonDeserialize(using = StringListToCommaSeparatedDeserializer.class)
    private String marketSegment;
    private String url;
}
