package com.work.truetech.services;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ICatgeoryService {
    Category createCategory(Category category);
    List<Category> retrieveCategory();
    Category getCategoryById(Long categoryId);
    Category updateCategory(Long categoryId, Category updatedCategory);
    void deleteCategory(Long id);
}
