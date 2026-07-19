package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.GenerateLinkedinNoteRequest;
import com.sharkdom.agenticai.model.SendConnectionRequestRequest;
import java.util.Map;

public interface LinkedInOutreachService {
    Map<String, Object> getAccountStatus(String accountId);
    Map<String, Object> listConnections(String accountId);
    Map<String, Object> generateNote(GenerateLinkedinNoteRequest request);
    Map<String, Object> sendConnectionRequest(SendConnectionRequestRequest request);
}