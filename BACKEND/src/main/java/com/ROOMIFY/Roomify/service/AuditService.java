package com.ROOMIFY.Roomify.service;

import com.ROOMIFY.Roomify.model.AuditLog;
import com.ROOMIFY.Roomify.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String targetEntity, String targetId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actorEmail = (auth != null) ? auth.getName() : "SYSTEM";
        String actorRole = (auth != null) ? auth.getAuthorities().toString() : "SYSTEM";

        AuditLog log = new AuditLog(actorEmail, actorRole, action, targetEntity, targetId, details);
        auditLogRepository.save(log);
        System.out.println("Audit: " + actorEmail + " -> " + action + " on " + targetEntity + ":" + targetId);
    }
}
