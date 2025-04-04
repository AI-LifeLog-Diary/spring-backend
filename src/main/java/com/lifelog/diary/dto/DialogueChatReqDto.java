package com.lifelog.diary.dto;

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
    private Long user_id;
    private String nickname;
    private String description;
    private String traits;
    private String today_diary;
    private String user_input;
}
