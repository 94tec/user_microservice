package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.Product;
import com.warmUP.user_Auth.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    // Search by brand
    @GetMapping("/search")
    public List<Product> searchByBrand(@RequestParam String brand) {
        return productService.searchByBrand(brand);
    }

    // Filter by price range
    @GetMapping("/filter/price")
    public List<Product> filterByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {
        return productService.filterByPriceRange(minPrice, maxPrice);
    }

    // Filter by category
    @GetMapping("/filter/category")
    public List<Product> filterByCategory(@RequestParam Long categoryId) {
        return productService.filterByCategory(categoryId);
    }

    // Filter by availability
    @GetMapping("/filter/availability")
    public List<Product> filterByAvailability(@RequestParam int stockQuantity) {
        return productService.filterByAvailability(stockQuantity);
    }
    @GetMapping("/search-filter")
    public List<Product> searchAndFilter(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer stockQuantity) {
        return productService.searchAndFilter(brand, minPrice, maxPrice, categoryId, stockQuantity);
    }
}
