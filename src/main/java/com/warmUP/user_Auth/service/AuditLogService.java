package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.AuditLog;
import com.warmUP.user_Auth.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // ✅ Log user action
    public void logAction(String action, String username) {
        AuditLog log = new AuditLog();
        log.setAction(action); // e.g., "LOGIN", "LOGOUT", "UPDATE_PROFILE"
        log.setUsername(username); // Username of the user performing the action
        log.setTimestamp(LocalDateTime.now()); // Current timestamp
        auditLogRepository.save(log); // Save the log to the database
    }

    // ✅ Retrieve all logs (for auditing purposes)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    // ✅ Retrieve logs by username
    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }
}