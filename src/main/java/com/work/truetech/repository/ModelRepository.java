package com.work.truetech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.work.truetech.entity.Model;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByPhoneId(Long phoneId);
    Model findByTitle(String title);
}
