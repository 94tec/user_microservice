package com.warmUP.user_Auth.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCredentials {

    @Value("${DB_USERNAME}")
    private String username;

    @Value("${DB_PASSWORD}")
    private String password;

    public String getUsername() {
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("Database username is not configured.");
        }
        return username;
    }

    public String getPassword() {
        if (StringUtils.isBlank(password)) {
            throw new IllegalStateException("Database password is not configured.");
        }
        return password;
    }
}