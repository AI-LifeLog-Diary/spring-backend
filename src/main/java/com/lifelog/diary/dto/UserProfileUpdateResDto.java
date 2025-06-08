package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.Hobby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileUpdateResDto {

    private Long userId;
    private String authProvider;
    private String email;
    private String username;
    private String nickname;
    private LocalDate birth;
    private String role;
    private String gender;
    private String profileUrl;
    private List<Hobby> hobbyList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
