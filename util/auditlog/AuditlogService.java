package com.sharkdom.util.auditlog;

import com.sharkdom.entity.auditlog.AuditLog;
import com.sharkdom.repository.auditlog.AuditLogRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AuditlogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public boolean createLog(String username, long organizationId, String action, String subAction) {
        log.info("Util >> createLog called!");
        boolean status = false;
        Date dtNow = new Date();
        try {
            AuditLog auditLog=new AuditLog();
            auditLog.setUserName(username);
            auditLog.setOrganizationId(organizationId);
            auditLog.setAction(action);
            auditLog.setSubAction(subAction);
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);
            if (savedAuditLog != null) {
                status = true;
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    public List<AuditLog> getAuditLogs()
    {
        List<AuditLog> auditLogs = null;
        log.info("Util >> getAuditLogs called!");
        try {
            Long organizationId= Util.getOrgIdFromToken();
            auditLogs = auditLogRepository.findByOrganizationId(organizationId);
        }
        catch (Exception e)
        {
            log.info("Util >> getAuditLogs failed!");
            e.getStackTrace();
        }
        return auditLogs;
    }
}
