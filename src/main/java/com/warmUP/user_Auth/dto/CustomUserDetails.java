package com.warmUP.user_Auth.dto;

import com.warmUP.user_Auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert roles and permissions to GrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can customize this based on your application's requirements
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can customize this based on your application's requirements
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can customize this based on your application's requirements
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled(); // Use a field from the User entity to determine if the account is enabled
    }

    // Additional method to get the underlying User entity
    public User getUser() {
        return user;
    }
}