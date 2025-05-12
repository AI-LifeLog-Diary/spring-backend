package com.lifelog.diary.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResDto {
    private String followUpQuestion;
    private String diaryContent;
    private DiaryResDto diary;
}

