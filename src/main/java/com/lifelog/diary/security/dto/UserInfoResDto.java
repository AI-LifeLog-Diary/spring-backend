package com.lifelog.diary.security.dto;

import com.lifelog.diary.domain.enums.AuthProvider;
import com.lifelog.diary.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResDto {

    private String username;
    private AuthProvider authProvider;
    private String providerId;
    private Role role;

}
