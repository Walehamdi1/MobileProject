package com.work.truetech.services;

import com.work.truetech.entity.City;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private String username;
    private String password;
    @Getter
    private Long id;
    private Collection<? extends GrantedAuthority> authorities;
    @Getter
    private String email;
    @Getter
    private int phone;

    @Getter
    private String address;

    @Getter
    private City city; // Assuming city is a String, but if it's an enum, change to the appropriate type

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                             String email, int phone, Long id, String address, City city) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.email = email;
        this.phone = phone;
        this.id = id;
        this.address = address;
        this.city = city;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
