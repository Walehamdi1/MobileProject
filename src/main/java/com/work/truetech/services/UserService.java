package com.work.truetech.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements IUserService{

    @Autowired
    private UserRepository userRepository;
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }


    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User retrieveUserById(long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User toggleUserValidity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Toggle the valid field
        user.setValid(!user.isValid());
        return userRepository.save(user);
    }

}
