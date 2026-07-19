package com.sharkdom.entity.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;

public record PersonaVersionResponse(
        Long orgId,
        PersonaMode personaMode,
        Integer version,
        OverlapFrequency overlapFrequency,
        RecordType recordType,
        JsonNode overlapRecord
) {}