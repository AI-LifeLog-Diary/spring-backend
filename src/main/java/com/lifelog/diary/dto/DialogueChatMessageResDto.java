package com.lifelog.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DialogueChatMessageResDto {

    private List<MessageInfoDto> messages;
    private Long nextCursor;
    private boolean hasNextPage;

}
