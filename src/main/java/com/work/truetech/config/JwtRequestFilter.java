package com.work.truetech.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            String username;

            try {
                username = jwtUtil.extractUsername(jwtToken);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        // Set authentication context here
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtTokenExpiredException e) {
                // Handle expired token
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired");
                return; // stop further processing
            } catch (JwtTokenInvalidException e) {
                // Handle invalid token
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is invalid");
                return; // stop further processing
            } catch (Exception e) {
                // Handle other exceptions
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
                return; // stop further processing
            }
        }

        chain.doFilter(request, response);
    }

    private void sendJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"error\": \"" + message + "\"}");
            out.flush();
        }
    }
}
