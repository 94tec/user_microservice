package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.Product;
import com.warmUP.user_Auth.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Search by brand
    public List<Product> searchByBrand(String brand) {
        return productRepository.findByBrandContainingIgnoreCase(brand);
    }

    // Filter by price range
    public List<Product> filterByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // Filter by category
    public List<Product> filterByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Filter by availability
    public List<Product> filterByAvailability(int stockQuantity) {
        return productRepository.findByStockQuantityGreaterThan(stockQuantity);
    }

    public List<Product> searchAndFilter(
            String brand,
            Double minPrice,
            Double maxPrice,
            Long categoryId,
            Integer stockQuantity) {
        return productRepository.searchAndFilter(brand, minPrice, maxPrice, categoryId, stockQuantity);
    }

}