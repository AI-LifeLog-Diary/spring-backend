package com.lifelog.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DialogueChatSessionResDto {

    private Long sessionId;
    private Long userId;
    private LastMessageInfoDto lastMessageInfoDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
