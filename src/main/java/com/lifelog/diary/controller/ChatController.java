package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.dto.ChatMessageReqDto;
import com.lifelog.diary.dto.ChatMessageResDto;
import com.lifelog.diary.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto<ChatMessageResDto>> handleMessage(@RequestBody ChatMessageReqDto chatRequest) {
        ChatMessageResDto chatResponse = chatService.handleChatLogic(
                chatRequest.getUserId(),
                chatRequest.getConversation(),
                chatRequest.getCurrentDiary()
        );

        MetaResponseDto meta = new MetaResponseDto(Code.OK, "챗봇 응답 성공");
        ResponseDto<ChatMessageResDto> response = new ResponseDto<>(meta, List.of(chatResponse));

        return ResponseEntity.ok(response);
    }

}

