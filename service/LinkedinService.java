package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.LinkedInCookieConnectRequest;
import java.util.Map;

public interface LinkedinService {
    Map<String, Object> listAccounts();
    Map<String, Object> authenticate();
    Map<String, Object> reconnect();
    Map<String, Object> authenticateCookie(LinkedInCookieConnectRequest request);
    Map<String, Object> deleteAccount(String accountId);
}