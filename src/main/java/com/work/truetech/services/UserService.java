package com.work.truetech.services;

import com.work.truetech.config.JwtUtil;
import com.work.truetech.entity.*;
import com.work.truetech.repository.PasswordResetCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.work.truetech.repository.UserRepository;

import java.util.*;

@Service
public class UserService implements IUserService{

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;
    @Autowired
    MailService mailService;

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
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public ResponseEntity<?> updateProfil(User updatedUser) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // Get the username of the authenticated user

        // Fetch the user by username directly
        User existingUser = userRepository.findByUsername(currentUsername);

        if (existingUser != null) {
            // Update user details
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setCity(updatedUser.getCity());


            User savedUser = userRepository.save(existingUser);
            UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());

            String newToken = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(savedUser);
        } else {
            // If the user is not found, return an error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Utilisateur non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    public ResponseEntity<Map<String, String>> requestResetCode(Map<String, String> request) {
        String email = request.get("email");
        User user = userRepository.findByEmail(email);

        Map<String, String> response = new HashMap<>();

        if (user == null) {
            response.put("message", "Email non trouvé.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String resetCode = generateResetCode();
        saveResetCode(user, resetCode);

        mailService.sendEmail(user.getEmail(), "Votre code de réinitialisation", "Votre code de réinitialisation est: " + resetCode);

        response.put("message", "Code de réinitialisation envoyé à votre e-mail.");
        return ResponseEntity.ok(response);
    }

    private String generateResetCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void saveResetCode(User user, String resetCode) {
        PasswordResetCode passwordResetCode = new PasswordResetCode();
        passwordResetCode.setCode(resetCode);
        passwordResetCode.setUser(user);
        passwordResetCode.setExpiryDate(new Date(System.currentTimeMillis() + 10 * 60 * 1000)); // 10 minutes expiry
        passwordResetCodeRepository.save(passwordResetCode);
    }

    public ResponseEntity<Map<String, String>> verifyResetCode(Map<String, String> request) {
        String code = request.get("code");
        User user = userRepository.findByEmail(request.get("email"));

        Map<String, String> response = new HashMap<>();

        if (user == null) {
            response.put("message", "Email non trouvé.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        PasswordResetCode resetCode = passwordResetCodeRepository.findByUser(user);
        if (resetCode == null || !resetCode.getCode().equals(code) || resetCode.getExpiryDate().before(new Date())) {
            response.put("message", "Code de réinitialisation non valide ou expiré.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("message", "Code de réinitialisation vérifié. Procédez à la réinitialisation du mot de passe.");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> resetPassword(Map<String, String> request) {
        String newPassword = request.get("newPassword");
        String email = request.get("email");

        Map<String, String> response = new HashMap<>();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.put("message", "Email non trouvé.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        PasswordResetCode resetCode = passwordResetCodeRepository.findByUser(user);
        if (resetCode != null) {
            passwordResetCodeRepository.delete(resetCode);
        }

        response.put("message", "Le mot de passe a été réinitialisé avec succès.");
        return ResponseEntity.ok(response);
    }


}
