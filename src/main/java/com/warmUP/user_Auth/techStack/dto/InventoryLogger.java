package com.warmUP.user_Auth.techStack.dto;

import com.warmUP.user_Auth.techStack.model.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryLogger {

    public void logPartCreation(Part part) {
        log.info("Inventory log: Part created - ID: {}, Name: {}, Stock: {}", part.getId(), part.getName(), part.getStock());
        // Additional logging logic can be added here
    }

    public void logPartUpdate(Part part) {
        log.info("Inventory log: Part updated - ID: {}, Name: {}, Stock: {}", part.getId(), part.getName(), part.getStock());
    }

    public void logPartDeletion(Long partId) {
        log.info("Inventory log: Part deleted - ID: {}", partId);
    }
}

