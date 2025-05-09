package com.lifelog.diary.dto;

import com.lifelog.diary.common.aes.AESUtil;
import com.lifelog.diary.domain.DialogueChat;
import com.lifelog.diary.domain.enums.ChatRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageInfoDto {

    private Long messageId;
    private Long sessionId;
    private Long userId;
    private String nickname;
    private ChatRole chatRole;
    private String message;
    private LocalDateTime createdAt;

    public static MessageInfoDto from(DialogueChat chat, AESUtil aesUtil) throws Exception {
        return MessageInfoDto.builder()
                .messageId(chat.getId())
                .sessionId(chat.getDialogueChatSession().getId())
                .userId(chat.getUser().getId())
                .nickname(chat.getUser().getNickname())
                .chatRole(chat.getChatRole())
                .message(aesUtil.decrypt(chat.getMessage()))
                .createdAt(chat.getCreatedAt())
                .build();
    }

}
