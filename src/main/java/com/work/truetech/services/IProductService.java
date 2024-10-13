package com.work.truetech.services;

import com.work.truetech.entity.Phone;
import com.work.truetech.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService {
    Product createProduct(Product product,Long categoryId, MultipartFile file) throws IOException;
    List<Product> retrieveProducts();

    Product updateProduct(Long phoneId, Product updatedProduct, MultipartFile file) throws IOException;
    void deleteProduct(Long id);
    Product retrieveProductById(Long id);
}
