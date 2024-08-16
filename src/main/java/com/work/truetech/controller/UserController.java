package com.work.truetech.controller;

import com.work.truetech.entity.Facture;
import com.work.truetech.services.IFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.work.truetech.entity.User;
import com.work.truetech.services.IUserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

    }

    @GetMapping("/find-all-users")
    @ResponseBody
    public List<User> getUsers() {
        List<User> listUser = userService.retrieveAllUsers();
        return listUser;
    }



    @GetMapping("/find-user/{userId}")
    @ResponseBody
    public User getUserById(@PathVariable("userId") long userId) {
        return  userService.retrieveUserById(userId);
    }

    @PutMapping("/update-user/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable("id") Long userId, @RequestBody User updatedUser) {
        return userService.updateUser(userId, updatedUser);
    }


    @DeleteMapping("/delete-user/{userId}")
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("/{id}/validity")
    public ResponseEntity<?> toggleUserValidity(@PathVariable Long id) {
        try {
            User updatedUser = userService.toggleUserValidity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User is now " + (updatedUser.isValid() ? "valid" : "invalid"));
            response.put("userId", String.valueOf(updatedUser.getId()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    @GetMapping("/find-all-factures")
    @ResponseBody
    public List<Facture> getFactures() {
        List<Facture> listFacture = factureService.retrieveAllFacture();
        return listFacture;
    }

}
