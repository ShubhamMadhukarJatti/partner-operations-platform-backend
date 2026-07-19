package com.sharkdom.util.auditlog;

import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogHelper {

    @Autowired
    private AuditlogService auditlogService;

    public void logAction(String action, String subAction) {
        try {
            String username = Util.getUserFromToken();
            Long orgId = Util.getOrgIdFromToken();
            auditlogService.createLog(username, orgId, action, subAction);
        } catch (Exception e) {
            log.error("Audit logging failed for action {}: {}", action, e.getMessage());
        }
    }

    public void logActionForOrgId(Long organizationId, String action, String subAction) {
        try {
            String username = Util.getUserFromToken();
            auditlogService.createLog(username, organizationId, action, subAction);
        } catch (Exception e) {
            log.error("Audit logging failed for action {}: {}", action, e.getMessage());
        }
    }
}