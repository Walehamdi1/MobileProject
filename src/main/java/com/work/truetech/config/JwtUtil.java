package com.work.truetech.config;

import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @Autowired
    UserRepository userRepository;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.token.expiration}")
    private long tokenExpiration;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());

        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé pour le nom d'utilisateur : " + userDetails.getUsername());
        }

        Map<String, Object> claims = new HashMap<>();

        if (userDetails.getAuthorities() != null) {
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        // Add email, address, city, and phone to the claims
        if (user.getEmail() != null) {
            claims.put("email", user.getEmail());
        }
        if (user.getAddress() != null) {
            claims.put("address", user.getAddress());
        }
        if (user.getCity() != null) {
            claims.put("city", user.getCity()); // Convert enum to string
        }
        claims.put("phone", user.getPhone());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException("Le jeton JWT a expiré", e);
        } catch (JwtException e) {
            throw new JwtTokenInvalidException("JWT token is invalid", e);
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return (String) claims.get("email");
    }

    public Integer extractPhone(String token) {
        Claims claims = extractClaims(token);
        return (Integer) claims.get("phone");
    }

    public Collection<?> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return (Collection<?>) claims.get("roles");
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateRefreshToken(UserDetails userDetails) {
        // Retrieve the User entity using the username from userDetails
        User user = userRepository.findByUsername(userDetails.getUsername());

        if (user == null) {
            throw new RuntimeException("User not found for username: " + userDetails.getUsername());
        }

        Map<String, Object> claims = new HashMap<>();

        // Add roles to claims if not null
        if (userDetails.getAuthorities() != null) {
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        // Add email to claims if not null
        if (user.getEmail() != null) {
            claims.put("email", user.getEmail());
        }

        // Add phone to claims if not 0 (assuming 0 is an invalid phone number)
        if (user.getPhone() != 0) {
            claims.put("phone", user.getPhone());
        }

        // Add address to claims if not null
        if (user.getAddress() != null) {
            claims.put("address", user.getAddress());
        }

        // Add city to claims if not null
        if (user.getCity() != null) {
            claims.put("city", user.getCity()); // Assuming city is an enum, we store the name of the enum
        }

        // Generate and return the refresh token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateRefreshToken(String token) {
        final String username = extractUsernameFromRefreshToken(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        return !isTokenExpired(token);
    }

    public String extractUsernameFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
