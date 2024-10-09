package com.work.truetech.repository;

import com.work.truetech.entity.Option;
import com.work.truetech.entity.PasswordResetCode;
import com.work.truetech.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetCodeRepository  extends JpaRepository<PasswordResetCode, Long> {
    PasswordResetCode findByUser(User user);
    @Transactional
    void deleteByUser(User user);
}
