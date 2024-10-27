package com.work.truetech.repository;

import com.work.truetech.entity.FactureOption;
import com.work.truetech.entity.FactureProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactureProductRepository extends JpaRepository<FactureProduct, Long> {
}
