package com.warmUP.user_Auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenDTO {
    private Long id;
    private String tokenValue;
    private LocalDateTime expiryTime;
    private Long user_id;
}

