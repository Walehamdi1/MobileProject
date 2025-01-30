package com.work.truetech.services;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Product;
import com.work.truetech.entity.SousCategorie;
import com.work.truetech.entity.User;
import com.work.truetech.repository.CategoryRepository;
import com.work.truetech.repository.ProductRepository;
import com.work.truetech.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public String getProductImagePath() {
        return uploadPath + "/products";
    }

    @Override
    public Product createProduct(Product product,Long categoryId, MultipartFile file) throws IOException {

        Category category = categoryRepository.findById(categoryId).get();
        String uploadDir = getProductImagePath();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User non trouvé avec l'ID: " + userDetails.getId()));

        product.setUser(user);

        Product savedProduct = productRepository.save(product);

        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        if (file != null && !file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String newFileName = savedProduct.getId() + "_" + originalFileName;

            fileStorageService.saveFile(file, newFileName, uploadDir);
            savedProduct.setImage(newFileName);
            savedProduct.setCategory(category);
        }

        return productRepository.save(savedProduct);
    }


    @Override
    public Page<Product> retrieveProductFilter(String filter, int page, String search, int size) {
        Pageable pageable = PageRequest.of(page, size);

        String categoryFilter = (filter.equalsIgnoreCase("all")) ? null : normalizeFilter(filter);
        String searchFilter = search.isEmpty() ? null : search;

        return productRepository.findProducts(categoryFilter, searchFilter, pageable);
    }

    private String normalizeFilter(String filter) {
        if (filter == null) return null;

        return filter
                .toLowerCase()
                .replaceAll("[Ééèêë]", "e")  // Handle accents
                .replaceAll("&", " and ")    // Match MySQL format for '&'
                .trim();
    }


    @Override
    public Product updateProduct(Long productId, Product updatedProduct, MultipartFile file) throws IOException {
        String uploadDir = getProductImagePath();

        Optional<Product> existingProductOpt = productRepository.findById(productId);
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();

            existingProduct.setTitle(updatedProduct.getTitle() != null ? updatedProduct.getTitle() : existingProduct.getTitle());
            existingProduct.setColors(updatedProduct.getColors() != null ? updatedProduct.getColors() : existingProduct.getColors());
            existingProduct.setQuantity(updatedProduct.getQuantity());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setDescription(updatedProduct.getDescription());

            if (file != null && !file.isEmpty()) {
                if (existingProduct.getImage() != null) {
                    File oldFile = new File(uploadDir, existingProduct.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                String originalFileName = file.getOriginalFilename();
                String newFileName = existingProduct.getId() + "_" + originalFileName;
                fileStorageService.saveFile(file, newFileName, uploadDir);
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
    public List<Product> getProductsByCategory(Category category) {
            return productRepository.findByCategory(category);
    }

    @Override
    public void deleteProduct(Long id) {
        String uploadDir = getProductImagePath();

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();

            if (existingProduct.getImage() != null) {
                File imageFile = new File(uploadDir, existingProduct.getImage());
                if (imageFile.exists() && !imageFile.delete()) {
                    System.err.println("Impossible de supprimer le fichier image: " + imageFile.getPath());
                }
            }
            productRepository.delete(existingProduct);
        } else {
            throw new RuntimeException("Produit non trouvé avec ID: " + id);
        }
    }
}
