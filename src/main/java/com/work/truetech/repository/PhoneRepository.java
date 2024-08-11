package com.work.truetech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.work.truetech.entity.Phone;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    Phone findByTitle(String title);
}
