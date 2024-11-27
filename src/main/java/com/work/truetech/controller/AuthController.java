package com.work.truetech.controller;

import com.work.truetech.config.JwtTokenExpiredException;
import com.work.truetech.config.JwtTokenInvalidException;
import com.work.truetech.config.JwtUtil;
import com.work.truetech.entity.PasswordResetCode;
import com.work.truetech.repository.PasswordResetCodeRepository;
import com.work.truetech.services.CustomUserDetails;
import com.work.truetech.services.MailService;
import com.work.truetech.services.UserService;
import jakarta.mail.MessagingException;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    MailService mailService;
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        User user = userRepository.findByUsername(authenticationRequest.getUsername());

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur non trouvé");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered.");
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nom d'utilisateur ou mot de passe incorrect !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!user.isValid()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Votre compte n'est toujours pas vérifié.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        final String jwt = jwtUtil.generateToken((CustomUserDetails) userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken((CustomUserDetails) userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("refreshToken", refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom d'utilisateur est déjà pris");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            mailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), user.getPassword());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de l'envoi de l'email.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == Role.USER) {
            user.setValid(true);
        }
        userRepository.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur enregistré avec succès");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le jeton d'actualisation est manquant");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Jeton d'actualisation non valide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String username = jwtUtil.extractUsernameFromRefreshToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String newAccessToken = jwtUtil.generateToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("token", newAccessToken);

            return ResponseEntity.ok(response);
        } catch (JwtTokenExpiredException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le jeton d'actualisation a expiré");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (JwtTokenInvalidException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Jeton d'actualisation non valide");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Une erreur s'est produite lors du traitement du jeton d'actualisation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> requestResetCode(@RequestBody Map<String, String> request) {
        return userService.requestResetCode(request);
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyResetCode(@RequestBody Map<String, String> request) {
        return userService.verifyResetCode(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        return userService.resetPassword(request);
    }

}
