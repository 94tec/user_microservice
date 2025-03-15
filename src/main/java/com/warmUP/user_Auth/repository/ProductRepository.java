package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Search by brand
    List<Product> findByBrandContainingIgnoreCase(String brand);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStockQuantityGreaterThan(int stockQuantity);

    List<Product> findByPriceRange(double minPrice, double maxPrice);

    @Query("SELECT p FROM Product p WHERE " +
            "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:stockQuantity IS NULL OR p.stockQuantity > :stockQuantity)")
    List<Product> searchAndFilter(
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("stockQuantity") Integer stockQuantity);
}
