package com.lifelog.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileUpdateResDto {

    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
