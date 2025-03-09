package com.warmUP.user_Auth.config;

import com.warmUP.user_Auth.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvVarChecker implements CommandLineRunner {

    @Value("${DB_USERNAME}")
    private String dbUsername;

    private final DatabaseService databaseService;

    @Autowired
    public EnvVarChecker(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(databaseService + "  Database username "+ dbUsername);
    }
}
