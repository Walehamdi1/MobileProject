package com.work.truetech.repository;

import com.work.truetech.entity.FactureOption;
import com.work.truetech.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FactureOptionRepository extends JpaRepository<FactureOption, Long> {
    @Query("SELECT o.model.phone.title AS phoneTitle, SUM(fo.quantity) AS totalQuantity " +
            "FROM FactureOption fo " +
            "JOIN fo.option o " +
            "JOIN o.model m " +
            "JOIN m.phone p " +
            "GROUP BY p.title")
    List<Object[]> findTotalOptionsBoughtByPhone();
}
