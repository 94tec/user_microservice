package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.AuditLogDTO;
import com.warmUP.user_Auth.model.AuditLog;
import com.warmUP.user_Auth.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);

    @Autowired
    private AuditLogService auditLogService;

    // ✅ Retrieve all audit logs
    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> getAllLogs() {
        try {
            logger.info("Received request to retrieve all audit logs");
            List<AuditLogDTO> logs = auditLogService.getAllLogs();
            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ✅ Retrieve audit logs by username
    @GetMapping("/username/{username}")
    public ResponseEntity<List<AuditLogDTO>> getLogsByUsername(@PathVariable String username) {
        try {
            logger.info("Received request to retrieve audit logs for username: {}", username);
            List<AuditLogDTO> logs = auditLogService.getLogsByUsername(username);
            return ResponseEntity.ok(logs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs for username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
