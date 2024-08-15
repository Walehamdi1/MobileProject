package com.work.truetech.repository;

import com.work.truetech.entity.Facture;
import com.work.truetech.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
}
