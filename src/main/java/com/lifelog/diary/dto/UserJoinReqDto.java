package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserJoinReqDto {

    private String nickname;
    private LocalDate birth;
    private Gender gender;
    private List<Hobby> hobbyList;

}
