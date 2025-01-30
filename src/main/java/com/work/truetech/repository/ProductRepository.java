package com.work.truetech.repository;

import com.work.truetech.entity.Category;
import com.work.truetech.entity.Phone;
import com.work.truetech.entity.Product;
import com.work.truetech.entity.SousCategorie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByTitle(String title);
    List<Product> findByCategory(Category category);
    @Query("SELECT p FROM Product p WHERE " +
            "(:filter IS NULL OR LOWER(REPLACE(p.category.name, '&', ' and ')) LIKE LOWER(CONCAT('%', :filter, '%'))) " +
            "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findProducts(@Param("filter") String filter,
                               @Param("search") String search,
                               Pageable pageable);




}
