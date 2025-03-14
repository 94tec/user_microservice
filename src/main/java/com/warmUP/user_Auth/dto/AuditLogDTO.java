package com.warmUP.user_Auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    private Long id;
    private String action;
    private String username;
    private LocalDateTime timestamp;
    private Long user_id;
}