package com.lifelog.diary.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReqDto {
    private Long userId;
    private String conversation;
    private String currentDiary;
}
