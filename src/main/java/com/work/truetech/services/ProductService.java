package com.work.truetech.services;

import com.work.truetech.entity.Product;
import com.work.truetech.entity.User;
import com.work.truetech.repository.ProductRepository;
import com.work.truetech.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${upload.path}")
    private String uploadPath;

    public String getProductImagePath() {
        return uploadPath + "/products";
    }

    @Override
    public Product createProduct(Product product, MultipartFile file) throws IOException {
        String uploadDir = getProductImagePath();

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Find the User by ID
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userDetails.getId()));

        // Associate the user with the product (if needed)
        //product.setUser(user);

        // Save the product first to generate an ID
        Product savedProduct = productRepository.save(product);

        // Ensure the upload directory exists
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String newFileName = savedProduct.getId() + "_" + originalFileName;

            // Save the file to the server
            fileStorageService.saveFile(file, newFileName, uploadDir);

            // Save the new filename to the product entity
            savedProduct.setImage(newFileName);
        }

        return productRepository.save(savedProduct);
    }

    @Override
    public List<Product> retrieveProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Long productId, Product updatedProduct, MultipartFile file) throws IOException {
        String uploadDir = getProductImagePath();

        // Retrieve the existing product
        Optional<Product> existingProductOpt = productRepository.findById(productId);
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();

            // Update fields if they are provided
            existingProduct.setTitle(updatedProduct.getTitle() != null ? updatedProduct.getTitle() : existingProduct.getTitle());
            existingProduct.setColor(updatedProduct.getColor() != null ? updatedProduct.getColor() : existingProduct.getColor());
            existingProduct.setQuantity(updatedProduct.getQuantity());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setPrice(updatedProduct.getPrice());

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Delete the old image file if it exists
                if (existingProduct.getImage() != null) {
                    File oldFile = new File(uploadDir, existingProduct.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Generate the new filename
                String originalFileName = file.getOriginalFilename();
                String newFileName = existingProduct.getId() + "_" + originalFileName;

                // Save the new file
                fileStorageService.saveFile(file, newFileName, uploadDir);

                // Update the product entity with the new filename
                existingProduct.setImage(newFileName);
            }

            return productRepository.save(existingProduct);
        } else {
            throw new EntityNotFoundException("Produit non trouvé avec l'ID: " + productId);
        }
    }

    @Override
    public Product retrieveProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID:" + id));
    }

    @Override
    public void deleteProduct(Long id) {
        String uploadDir = getProductImagePath();

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();

            // Delete the associated image file if it exists
            if (existingProduct.getImage() != null) {
                File imageFile = new File(uploadDir, existingProduct.getImage());
                if (imageFile.exists() && !imageFile.delete()) {
                    System.err.println("Impossible de supprimer le fichier image: " + imageFile.getPath());
                }
            }

            // Delete the product from the repository
            productRepository.delete(existingProduct);
        } else {
            throw new RuntimeException("Produit non trouvé avec ID: " + id);
        }
    }
}
