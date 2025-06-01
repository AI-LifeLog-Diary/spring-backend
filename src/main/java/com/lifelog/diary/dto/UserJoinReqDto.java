package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class UserJoinReqDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
    @NotNull(message = "생일은 필수입니다.")
    private LocalDate birth;
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;
    @NotNull(message = "취미 선택은 필수입니다.")
    private List<Hobby> hobbyList;

}
