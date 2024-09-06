package com.work.truetech.controller;

import com.work.truetech.entity.Facture;
import com.work.truetech.services.IFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.work.truetech.entity.User;
import com.work.truetech.services.IUserService;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class UserController {
    @Autowired
    IUserService userService;
    @Autowired
    IFactureService factureService;

    @PostMapping("/add-user")
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user) {

        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }

    }

    @GetMapping("/find-all-users")
    @ResponseBody
    public List<User> getUsers() {
        try {
            List<User> listUser = userService.retrieveAllUsers();
            return listUser;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }



    @GetMapping("/find-user/{userId}")
    @ResponseBody
    public User getUserById(@PathVariable("userId") long userId) {
        try {

            return  userService.retrieveUserById(userId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @PutMapping("/update-user/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable("id") Long userId, @RequestBody User updatedUser) {
        try {
            return userService.updateUser(userId, updatedUser);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }


    @DeleteMapping("/delete-user/{userId}")
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId) {

        try {
            userService.deleteUser(userId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @PutMapping("/{id}/validity")
    public ResponseEntity<?> toggleUserValidity(@PathVariable Long id) {
        try {
            User updatedUser = userService.toggleUserValidity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User is now " + (updatedUser.isValid() ? "valid" : "invalid"));
            response.put("userId", String.valueOf(updatedUser.getId()));
            return ResponseEntity.ok(response);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }  catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    @GetMapping("/find-all-factures")
    @ResponseBody
    public List<Facture> getFactures() {
        try {
            List<Facture> listFacture = factureService.retrieveAllFacture();
            return listFacture;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        try {
            long count = userService.countAllUsers();
            // Return the count as a JSON object
            Map<String, Long> response = Collections.singletonMap("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return an error message as a JSON object
            Map<String, String> errorResponse = Collections.singletonMap("error", "Error retrieving user count: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
