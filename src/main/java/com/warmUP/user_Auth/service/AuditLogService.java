// AuditLogService.java
package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.AuditLogDTO;
import com.warmUP.user_Auth.exception.ServiceException;
import com.warmUP.user_Auth.model.AuditLog;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.AuditLogRepository;
import com.warmUP.user_Auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logAction(String action, String username) {
        try {
            userRepository.findByUsername(username)
                    .ifPresentOrElse(
                            user -> {
                                AuditLog log = new AuditLog();
                                log.setUser(user);
                                log.setAction(action);
                                log.setUsername(user.getUsername());
                                log.setTimestamp(LocalDateTime.now());
                                log.setUser_id(user.getId());

                                try {
                                    auditLogRepository.save(log);
                                    logger.info("Audit log saved successfully for user: {}, action: {}", username, action);
                                } catch (DataAccessException e) {
                                    logger.error("Error saving audit log for user: {}, action: {}", username, action, e);
                                }
                            },
                            () -> logger.warn("User not found: {}", username)
                    );
        } catch (Exception e) {
            logger.error("An unexpected error occurred while logging action for user: {}", username, e);
        }
    }

    public List<AuditLogDTO> getAllLogs() {
        try {
            logger.info("Retrieving all audit logs");
            List<AuditLog> logs = auditLogRepository.findAll();

            if (logs.isEmpty()) {
                logger.info("No audit logs found");
                return Collections.emptyList();
            }

            logger.info("Successfully retrieved {} audit logs", logs.size());
            return logs.stream().map(this::convertToDTO).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs", e);
            throw new ServiceException("Failed to retrieve audit logs. Please try again.");
        }
    }

    public List<AuditLogDTO> getLogsByUsername(String username) {
        try {
            logger.info("Retrieving audit logs for username: {}", username);

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
            return logs.stream().map(this::convertToDTO).collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("Failed to retrieve audit logs for username: {}", username, e);
            throw new ServiceException("Failed to retrieve audit logs for username: " + username);
        }
    }

    private AuditLogDTO convertToDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        BeanUtils.copyProperties(log, dto);
        return dto;
    }
}