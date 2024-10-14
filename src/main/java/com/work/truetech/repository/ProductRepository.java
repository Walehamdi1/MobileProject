package com.work.truetech.repository;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Product;
import com.work.truetech.entity.SousCategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByTitle(String title);
    List<Product> findByCategory(Category category);
    List<Product> findBySousCategorie(SousCategorie sousCategorie);

}
