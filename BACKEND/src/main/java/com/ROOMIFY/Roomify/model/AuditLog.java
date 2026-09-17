package com.ROOMIFY.Roomify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorEmail;
    private String actorRole;
    private String action;
    private String targetEntity;
    private String targetId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    private LocalDateTime timestamp;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String actorEmail, String actorRole, String action, String targetEntity, String targetId, String details) {
        this();
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.action = action;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.details = details;
    }
}
