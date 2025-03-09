package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.config.DatabaseCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private final DatabaseCredentials databaseCredentials;

    @Autowired
    public DatabaseService(DatabaseCredentials databaseCredentials) {
        this.databaseCredentials = databaseCredentials;
    }

    public void connectToDatabase() {
        String username = databaseCredentials.getUsername();
        String password = databaseCredentials.getPassword();

        // Use the credentials to connect to the database
        System.out.println("Connecting to database with username: " + username);
    }
}
