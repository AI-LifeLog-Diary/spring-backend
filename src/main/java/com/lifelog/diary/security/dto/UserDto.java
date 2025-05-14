package com.lifelog.diary.security.dto;

import com.lifelog.diary.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private String username;
    private String name;
    private Role role;
    private String email;
}
