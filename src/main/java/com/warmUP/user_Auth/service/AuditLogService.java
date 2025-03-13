package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.exception.ServiceException;
import com.warmUP.user_Auth.model.AuditLog;
import com.warmUP.user_Auth.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AuditLogService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuditLogService.class);

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
        try {
            logger.info("Retrieving all audit logs");
            List<AuditLog> logs = auditLogRepository.findAll();

            if (logs.isEmpty()) {
                logger.info("No audit logs found");
                return Collections.emptyList();
            }

            logger.info("Successfully retrieved {} audit logs", logs.size());
            return logs;

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs", e);
            throw new ServiceException("Failed to retrieve audit logs. Please try again.");
        }
    }

    // ✅ Retrieve logs by username
    public List<AuditLog> getLogsByUsername(String username) {
        try {
            logger.info("Retrieving audit logs for username: {}", username);

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                logger.error("Username cannot be null or empty");
                throw new IllegalArgumentException("Username cannot be null or empty");
            }

            List<AuditLog> logs = auditLogRepository.findByUsername(username);

            if (logs.isEmpty()) {
                logger.info("No audit logs found for username: {}", username);
                return Collections.emptyList();
            }

            logger.info("Successfully retrieved {} audit logs for username: {}", logs.size(), username);
            return logs;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e; // Re-throw the exception

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs for username: {}", username, e);
            throw new ServiceException("Failed to retrieve audit logs for username: " + username);
        }
    }
}