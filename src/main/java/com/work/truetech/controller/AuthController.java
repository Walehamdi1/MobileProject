package com.work.truetech.controller;

import com.work.truetech.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.work.truetech.dto.AuthenticationRequest;
import com.work.truetech.dto.AuthenticationResponse;
import com.work.truetech.entity.Role;
import com.work.truetech.entity.User;

import com.work.truetech.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        User user = userRepository.findByUsername(authenticationRequest.getUsername());

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Incorrect username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (!user.isValid()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Your account is still not verified.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        final String jwt = jwtUtil.generateToken(userDetails); // No need to pass the User entity here

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }




    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) throws Exception {
        Map<String, Object> response = new HashMap<>();

        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()) != null) {
            response.put("status", "error");
            response.put("message", "Username already exists, please choose a different one.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Save the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.SUPPLIER);
        user.setValid(false);
        userRepository.save(user);

        // Return success response
        response.put("status", "success");
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

/*
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }*/
}
