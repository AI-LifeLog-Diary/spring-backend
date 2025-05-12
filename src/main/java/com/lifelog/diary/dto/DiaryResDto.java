package com.lifelog.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResDto {
    private Long diaryId;
    private Long userId;
    private String content;
    private String createdAt;
}
