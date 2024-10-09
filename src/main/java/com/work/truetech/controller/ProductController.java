package com.work.truetech.controller;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Product;
import com.work.truetech.services.IProductService;
import com.work.truetech.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/admin/product/add-product")
    @ResponseBody
    public ResponseEntity<?> createProduct(@RequestParam("title") String title,
                                           @RequestParam("color") String color,
                                           @RequestParam("quantity") int quantity,
                                           @RequestParam("category") Category category,
                                           @RequestParam("file") MultipartFile file) {
        try {
            // Check if a product with the same title exists
            if (productRepository.findByTitle(title) != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Le nom du produit existe déjà, veuillez en choisir un autre.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create a new product
            Product product = new Product();
            product.setTitle(title);
            product.setColor(color);
            product.setQuantity(quantity);
            product.setCategory(category);  // Assuming category is a String for simplicity; adjust as needed

            Product createdProduct = productService.createProduct(product, file);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/product/find-all-products")
    @ResponseBody
    public List<?> getProducts() {
        try {
            List<Product> listProduct = productService.retrieveProducts();
            return listProduct;
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
                                           @RequestParam(value = "color", required = false) String color,
                                           @RequestParam(value = "quantity", required = false) Integer quantity,
                                           @RequestParam(value = "category", required = false) Category category,
                                           @RequestParam(value = "file", required = false) MultipartFile file) {

        // Retrieve the existing product by ID
        Optional<Product> existingProductOpt = productRepository.findById(productId);
        if (!existingProductOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit avec id " + productId + " non trouvé");
        }

        Product existingProduct = existingProductOpt.get();

        // Perform duplicate name check if the title is being changed
        if (title != null && !title.equals(existingProduct.getTitle()) && productRepository.findByTitle(title) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom du produit existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Create an updated Product object with the provided details
            Product productToUpdate = new Product();
            productToUpdate.setTitle(title != null ? title : existingProduct.getTitle());
            productToUpdate.setColor(color != null ? color : existingProduct.getColor());
            productToUpdate.setQuantity(quantity != null ? quantity : existingProduct.getQuantity());
            productToUpdate.setCategory(category != null ? category : existingProduct.getCategory());

            // Call the service to update the product
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
}
