package com.work.truetech.repository;

import com.work.truetech.entity.Option;
import com.work.truetech.entity.PasswordResetCode;
import com.work.truetech.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetCodeRepository  extends JpaRepository<PasswordResetCode, Long> {
    PasswordResetCode findByUser(User user);
}
