package com.work.truetech.services;

import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.entity.User;

import java.util.List;

public interface IUserService {
    User createUser(User user);
    public void deleteUser(long id);
    public User retrieveUserById(long id);
    List<User> retrieveAllUsers();
    public User toggleUserValidity(Long userId);
}
