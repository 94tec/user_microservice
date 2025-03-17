package com.warmUP.user_Auth.techStack.controller;

import com.warmUP.user_Auth.exception.ServiceException;
import com.warmUP.user_Auth.techStack.model.Part;
import com.warmUP.user_Auth.techStack.service.PartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@PreAuthorize("hasRole('ADMIN')")
public class PartController {

    private final PartService partService;
    private static final Logger logger = LoggerFactory.getLogger(PartController.class);

    @Autowired
    public PartController(PartService partService) {
        this.partService = partService;
    }

    @PostMapping
    public ResponseEntity<Part> createPart(@Valid @RequestBody Part part) {
        try {
            Part createdPart = partService.createPart(part);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPart);
        } catch (ServiceException ex) {
            logger.error("Error creating part: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}



