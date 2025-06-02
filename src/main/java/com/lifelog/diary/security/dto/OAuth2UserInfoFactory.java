package com.lifelog.diary.security.dto;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2ResDto getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfoResDto(attributes);
        } else if (registrationId.equalsIgnoreCase("apple")) {
            return new AppleOAuth2UserInfoResDto(attributes);
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다 : " + registrationId);
        }
    }
}
