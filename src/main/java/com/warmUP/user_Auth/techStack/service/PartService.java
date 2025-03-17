package com.warmUP.user_Auth.techStack.service;

import com.warmUP.user_Auth.exception.ServiceException;
import com.warmUP.user_Auth.techStack.dto.InventoryLogger;
import com.warmUP.user_Auth.techStack.model.Part;
import com.warmUP.user_Auth.techStack.repository.PartRepository;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PartService {

    private final PartRepository partRepository;
    private final InventoryLogger inventoryLogger;

    @Autowired
    public PartService(PartRepository partRepository, InventoryLogger inventoryLogger) {
        this.partRepository = partRepository;
        this.inventoryLogger = inventoryLogger;
    }

    public Part createPart(Part part) {
        try {
            validatePart(part);
            Part savedPart = partRepository.save(part);
            log.info("Part created successfully: {}", savedPart.getName());
            inventoryLogger.logPartCreation(savedPart);
            return savedPart;
        } catch (ValidationException ex) {
            log.error("Validation error creating part: {}", ex.getMessage());
            throw new ServiceException("Could not create part due to validation errors", ex);
        } catch (Exception ex) {
            log.error("Unexpected error creating part: {}", ex.getMessage(), ex);
            throw new ServiceException("Unexpected error creating part", ex);
        }
    }

    private void validatePart(Part part) {
        if (part.getName() == null || part.getName().isEmpty()) {
            throw new ValidationException("Part name is required");
        }
        if (part.getPrice() <= 0) {
            throw new ValidationException("Price must be a positive value");
        }
        if (part.getStock() < 0) {
            throw new ValidationException("Stock cannot be negative");
        }
    }
}


