package com.work.truetech.controller;

import com.work.truetech.entity.Category;
import com.work.truetech.services.CatgeoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryController {

    @Autowired
    private CatgeoryService categoryService;

    @PostMapping("/admin/category/create")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category createdCategory = categoryService.createCategory(category);
            response.put("status", "success");
            response.put("message", "Categorie ajouter avec success !");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/api/category/find")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.retrieveCategory();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/api/category/find/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/admin/category/update/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable("id") Long categoryId, @RequestBody Category updatedCategory) {
        Map<String, Object> response = new HashMap<>();
        try {
            categoryService.updateCategory(categoryId, updatedCategory);
            response.put("status", "success");
            response.put("message", "Categorie modifier avec success !");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @DeleteMapping("/admin/category/delete/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
