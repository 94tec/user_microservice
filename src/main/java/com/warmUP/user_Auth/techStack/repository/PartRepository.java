package com.warmUP.user_Auth.techStack.repository;

import com.warmUP.user_Auth.techStack.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, Long> {

    // Custom query methods can be added here

    // Example: Find parts by category
    List<Part> findByCategory(String category);

    // Example: Find parts by manufacturer
    List<Part> findByManufacturer(String manufacturer);

    // Example: Find parts by model compatibility
    List<Part> findByModelCompatibility(String modelCompatibility);
}


