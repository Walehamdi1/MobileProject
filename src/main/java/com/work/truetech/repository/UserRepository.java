package com.work.truetech.repository;

import com.work.truetech.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.work.truetech.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.role = :userRole OR u.role = :supplierRole")
    List<User> findUsersByRole(@Param("userRole") Role userRole, @Param("supplierRole") Role supplierRole);
}
