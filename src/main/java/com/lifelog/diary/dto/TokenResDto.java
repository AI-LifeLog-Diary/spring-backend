package com.lifelog.diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResDto {

    private String accessToken;
    private String refreshToken;
}
