package com.work.truetech.repository;

import com.stripe.model.Invoice;
import com.work.truetech.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> , JpaSpecificationExecutor<Facture> {
}
