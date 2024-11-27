package com.work.truetech.services;

import com.work.truetech.entity.Category;
import com.work.truetech.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatgeoryService implements ICatgeoryService{
    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Nom Category existe déja.");
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> retrieveCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).get();
    }

    @Override
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categorie non trouvé: " + categoryId));

        if (categoryRepository.existsByName(updatedCategory.getName()) &&
                !existingCategory.getName().equals(updatedCategory.getName())) {
            throw new IllegalArgumentException("Nom Categorie existe déja.");
        }

        existingCategory.setName(updatedCategory.getName());
        return categoryRepository.save(existingCategory);
    }



    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
