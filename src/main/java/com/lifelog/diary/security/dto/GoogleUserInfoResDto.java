package com.lifelog.diary.security.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class GoogleUserInfoResDto implements OAuth2ResDto {

    private final Map<String,Object> attribute;

    @Override
    public String getProvider() {
        return "google";
    }
    @Override
    public String getProviderId() {
        return (String) attribute.get("sub");
    }
    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }
    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

}

