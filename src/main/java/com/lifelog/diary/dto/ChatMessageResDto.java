package com.lifelog.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResDto {

    private String followUpQuestion;
    private String diaryContent;

    public static ChatMessageResDto of(String followUpQuestion, String diaryContent) {
        return new ChatMessageResDto(followUpQuestion, diaryContent);
    }
}