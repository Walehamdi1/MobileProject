package com.work.truetech.controller;

import com.work.truetech.config.JwtUtil;
import com.work.truetech.services.CustomUserDetails;
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
            response.put("message", "Utilisateur non trouvé");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }  catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nom d'utilisateur ou mot de passe incorrect !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (!user.isValid()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Votre compte n'est toujours pas vérifié.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        final String jwt = jwtUtil.generateToken((CustomUserDetails) userDetails); // No need to pass the User entity here
        final String refreshToken = jwtUtil.generateRefreshToken((CustomUserDetails) userDetails); // Generate refresh token

        // Return both tokens in the response
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }




    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) throws Exception {
        Map<String, Object> response = new HashMap<>();

        try {

            // Check if username already exists
            if (userRepository.findByUsername(user.getUsername()) != null) {
                response.put("status", "erreur");
                response.put("message", "Le nom d'utilisateur existe déjà, veuillez en choisir un autre.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Save the user
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(Role.SUPPLIER);
            user.setValid(false);
            userRepository.save(user);

            // Return success response
            response.put("status", "succès");
            response.put("message", "Utilisateur enregistré avec succès!");
            return ResponseEntity.ok(response);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
