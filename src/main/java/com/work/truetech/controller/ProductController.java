package com.work.truetech.controller;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Product;
import com.work.truetech.services.IProductService;
import com.work.truetech.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/admin/product/{categoryId}/add-product")
    @ResponseBody
    public ResponseEntity<?> createProduct(@RequestParam("title") String title,
                                           @PathVariable("categoryId") Long categoryId,
                                           @RequestParam("color") List<String> color,
                                           @RequestParam("quantity") int quantity,
                                           @RequestParam("price") int price,
                                           @RequestParam("description") String description,
                                           @RequestParam("file") MultipartFile file) {
        try {
            if (productRepository.findByTitle(title) != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Le nom du produit existe déjà, veuillez en choisir un autre.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (file.getSize() > (50 * 1024 * 1024)) { // Example size limit: 50MB
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("message", "La taille de l'image dépasse la limite autorisée (5MB).")
                );
            }

            Product product = new Product();
            product.setTitle(title);
            product.setColors(color);
            product.setQuantity(quantity);
            product.setPrice(price);
            product.setDescription(description);

            Product createdProduct = productService.createProduct(product,categoryId, file);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/products")
    @ResponseBody
    public Page<Product> getProducts(
            @RequestParam(required = false, defaultValue = "all") String filter,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return productService.retrieveProductFilter(filter, page, search, size);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer les produits: " + e.getMessage(), e);
        }
    }



    @GetMapping("/api/product/find-product/{productId}")
    @ResponseBody
    public Product getProductById(@PathVariable("productId") long productId) {
        try {
            return productService.retrieveProductById(productId);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @PutMapping("/admin/product/update-product/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable("id") Long productId,
                                           @RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "color", required = false) List<String> color,
                                           @RequestParam(value = "quantity", required = false) Integer quantity,
                                           @RequestParam(value = "price", required = false) Integer price,
                                           @RequestParam(value = "category", required = false) Category category,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "file", required = false) MultipartFile file) {

        Optional<Product> existingProductOpt = productRepository.findById(productId);
        if (!existingProductOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit avec id " + productId + " non trouvé");
        }

        Product existingProduct = existingProductOpt.get();

        if (title != null && !title.equals(existingProduct.getTitle()) && productRepository.findByTitle(title) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom du produit existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Product productToUpdate = new Product();
            productToUpdate.setTitle(title != null ? title : existingProduct.getTitle());
            productToUpdate.setColors(color != null ? color : existingProduct.getColors());
            productToUpdate.setQuantity(quantity != null ? quantity : existingProduct.getQuantity());
            productToUpdate.setPrice(price != null ? price : existingProduct.getPrice());
            productToUpdate.setCategory(category != null ? category : existingProduct.getCategory());
            productToUpdate.setDescription(description != null ? description : existingProduct.getDescription());

            Product updatedProduct = productService.updateProduct(productId, productToUpdate, file);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/admin/product/delete-product/{id}")
    @ResponseBody
    public void deleteProduct(@PathVariable("id") Long productId) {
        try {
            productService.deleteProduct(productId);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @GetMapping("/api/product/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        Category category = new Category();
        category.setId(categoryId);

        return productService.getProductsByCategory(category);
    }

}
