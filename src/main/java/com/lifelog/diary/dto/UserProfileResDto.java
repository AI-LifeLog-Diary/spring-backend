package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.AuthProvider;
import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import com.lifelog.diary.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResDto {

    private Long userId;
    private AuthProvider authProvider;
    private String email;
    private String username;
    private String nickname;
    private LocalDate birth;
    private Role role;
    private Gender gender;
    private String profileUrl;
    private List<Hobby> hobbyList;
    private LocalDateTime createdAt;
}
