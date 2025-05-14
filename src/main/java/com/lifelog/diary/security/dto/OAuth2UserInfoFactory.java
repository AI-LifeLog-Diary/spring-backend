package com.lifelog.diary.security.dto;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2ResDto getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleUserInfoResDto(attributes);
            case "apple":
                return new AppleUserInfoResDto(attributes);
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }
}
