package com.lifelog.diary.security.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDto;

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", userDto.getUsername());
        attributes.put("email", userDto.getEmail());
        attributes.put("role", userDto.getRole());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userDto.getRole().name()));
    }

    @Override
    public String getName() {
        return userDto.getUsername();
    }

}
