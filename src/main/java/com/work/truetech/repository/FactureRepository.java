package com.work.truetech.repository;

import com.stripe.model.Invoice;
import com.work.truetech.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> , JpaSpecificationExecutor<Facture> {

    // Query to get total of Factures created in the last week
    @Query("SELECT SUM(f.total) FROM Facture f WHERE f.creationDate >= :startDate")
    Double findWeeklyTotal(LocalDateTime startDate);

    // Query to get total of Factures created in the last month
    @Query("SELECT SUM(f.total) FROM Facture f WHERE f.creationDate >= :startDate")
    Double findMonthlyTotal(LocalDateTime startDate);

    List<Facture> findAllByCreationDateBetween(LocalDateTime start, LocalDateTime end);
}
