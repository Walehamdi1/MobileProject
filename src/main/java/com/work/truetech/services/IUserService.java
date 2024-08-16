package com.work.truetech.services;

import com.work.truetech.entity.Facture;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserService {
    User createUser(User user);
    public void deleteUser(long id);
    public User retrieveUserById(long id);
    List<User> retrieveAllUsers();
    User toggleUserValidity(Long userId);
    ResponseEntity<?> updateUser(Long userId, User user);

}
