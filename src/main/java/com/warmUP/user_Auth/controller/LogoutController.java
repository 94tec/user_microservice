package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logout")
public class LogoutController {

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<Void> logout(@RequestParam String token) {
        tokenService.revokeToken(token);
        return ResponseEntity.noContent().build();
    }
}
