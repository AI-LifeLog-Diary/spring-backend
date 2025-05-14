package com.lifelog.diary.security.dto;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

public class AppleUserInfoResDto implements OAuth2ResDto {
    private final Map<String, Object> attributes;

    public AppleUserInfoResDto(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "apple";
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
        return (String) attributes.getOrDefault("name", "AppleUser");
    }

}
