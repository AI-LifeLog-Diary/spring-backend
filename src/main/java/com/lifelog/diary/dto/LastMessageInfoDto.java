package com.lifelog.diary.dto;

import com.lifelog.diary.domain.enums.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LastMessageInfoDto {

    private Long messageId;
    private ChatRole chatRole;
    private String message;
    private LocalDateTime createdAt;
}
