package com.sharkdom.service.ai;

import com.sharkdom.model.ai.OverlapRequest;
import com.sharkdom.model.ai.PersonaMode;

public interface OverlapDataProvider {

    PersonaMode getSource();

    OverlapRequest fetch(Long organizationId);
}