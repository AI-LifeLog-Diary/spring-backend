package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueChatReqDto {
    private Long userId;
    private String nickname;
    private Gender gender;
    private List<Hobby> hobby;
    private String todayDiary;
    private String userInput;
}
