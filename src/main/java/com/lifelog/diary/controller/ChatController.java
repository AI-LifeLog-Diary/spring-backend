package com.lifelog.diary.controller;

import com.lifelog.diary.dto.ChatMessageReqDto;
import com.lifelog.diary.dto.ChatMessageResDto;
import com.lifelog.diary.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessageResDto> streamDiaryFromConversation(@RequestBody ChatMessageReqDto reqDto) {
        return chatService.chatAndCreateDiary(
                reqDto.getUserId(),
                reqDto.getConversation(),
                reqDto.getCurrentDiary()
        );
    }
}
