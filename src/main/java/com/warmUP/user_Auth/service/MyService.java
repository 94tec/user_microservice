package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.config.DatabaseCredentials;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @Autowired
    private DatabaseCredentials credentials;

    public void doSomethingWithCredentials() {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        // Use the username and password...
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }
}
