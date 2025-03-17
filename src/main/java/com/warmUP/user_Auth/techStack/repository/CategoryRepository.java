package com.warmUP.user_Auth.techStack.repository;

import com.warmUP.user_Auth.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Custom queries can be added here
}

