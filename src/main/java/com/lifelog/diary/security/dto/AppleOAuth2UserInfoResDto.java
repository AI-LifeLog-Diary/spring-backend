package com.lifelog.diary.security.dto;

import com.lifelog.diary.domain.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AppleOAuth2UserInfoResDto implements OAuth2ResDto {

    private final Map<String, Object> attributes;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // Apple에서는 name이 없을 수 있음
        Map<String, Object> nameObj = (Map<String, Object>) attributes.get("name");
        if (nameObj != null) {
            return nameObj.getOrDefault("firstName", "") + " " + nameObj.getOrDefault("lastName", "");
        }
        return "AppleUser";
    }

}
