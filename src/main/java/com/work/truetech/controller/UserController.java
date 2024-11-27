package com.work.truetech.controller;

import com.work.truetech.dto.PasswordChangeRequest;
import com.work.truetech.entity.Facture;
import com.work.truetech.repository.UserRepository;
import com.work.truetech.services.IFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.work.truetech.entity.User;
import com.work.truetech.services.IUserService;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

@RestController
public class UserController {
    @Autowired
    IUserService userService;
    @Autowired
    IFactureService factureService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/admin/add-user")
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @GetMapping("/admin/find-all-users")
    @ResponseBody
    public List<User> getUsers() {
        try {
            List<User> listUser = userService.retrieveAllUsers();
            return listUser;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @GetMapping("/admin/find-user/{userId}")
    @ResponseBody
    public User getUserById(@PathVariable("userId") long userId) {
        try {

            return  userService.retrieveUserById(userId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @PutMapping("/api/update-profil")
    @ResponseBody
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser) {
        try {
            return userService.updateProfil(updatedUser);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @DeleteMapping("/admin/delete-user/{userId}")
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @PutMapping("/admin/{id}/validity")
    public ResponseEntity<?> toggleUserValidity(@PathVariable Long id) {
        try {
            User updatedUser = userService.toggleUserValidity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User est " + (updatedUser.isValid() ? "valid" : "invalid"));
            response.put("userId", String.valueOf(updatedUser.getId()));
            return ResponseEntity.ok(response);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }  catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/admin/find-all-factures")
    @ResponseBody
    public List<Facture> getFactures() {
        try {
            List<Facture> listFacture = factureService.retrieveAllFacture();
            return listFacture;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @GetMapping("/admin/count")
    public ResponseEntity<?> getUserCount() {
        try {
            long count = userService.countAllUsers();
            Map<String, Long> response = Collections.singletonMap("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = Collections.singletonMap("error", "Erreur lors de la récupération du nombre d'utilisateurs: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    @PutMapping("/api/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username);
        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "L'ancien mot de passe est incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mot de passe modifié avec succès");
        return ResponseEntity.ok(response);
    }
}
