package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.Category;
import com.warmUP.user_Auth.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

}

