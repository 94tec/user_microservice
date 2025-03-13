package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private UserActivityService userActivityService;

    @GetMapping("/is-active")
    public ResponseEntity<Boolean> isSessionActive(@RequestParam Long userId) {
        boolean isActive = userActivityService.isSessionActive(userId);
        return ResponseEntity.ok(isActive);
    }
}
