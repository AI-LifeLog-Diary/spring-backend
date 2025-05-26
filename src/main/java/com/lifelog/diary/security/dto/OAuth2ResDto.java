package com.lifelog.diary.security.dto;


import com.lifelog.diary.domain.enums.AuthProvider;

public interface OAuth2ResDto {

    AuthProvider getProvider();
    String getProviderId();
    String getEmail();
    String getName();

}
