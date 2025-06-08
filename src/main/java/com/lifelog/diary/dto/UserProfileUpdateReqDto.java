package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileUpdateReqDto {

    private String nickname;
    private LocalDate birth;
    private Gender gender;
    private List<Hobby> hobbyList;
}
