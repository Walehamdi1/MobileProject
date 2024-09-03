package com.work.truetech.services;

import com.work.truetech.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements IUserService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public User createUser(User user) {
        // Encode the password provided in the request
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set the default role and validity
        user.setRole(Role.SUPPLIER);
        user.setValid(false);

        // Save the user
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
        return userRepository.findUsersByRole(Role.USER,Role.SUPPLIER);
    }

    @Override
    public User toggleUserValidity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Toggle the valid field
        user.setValid(!user.isValid());
        return userRepository.save(user);
    }

    @Override
    public ResponseEntity<?> updateUser(Long id, User updatedUser) {
        Optional<User> existingUserOptional = userRepository.findById(id);

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setPhone(updatedUser.getPhone());

            // Encode password only if it's provided in the request
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            // Update phones if needed
            existingUser.setPhones(updatedUser.getPhones());

            User savedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(savedUser);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
