package com.lifelog.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserJoinResDto {

    private Long userId;
    private LocalDateTime createdAt;
}
