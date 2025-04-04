package com.lifelog.diary.controller;

import com.lifelog.diary.dto.DialogueChatReqDto;
import com.lifelog.diary.service.DialogueChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatbot")
public class ChatController {

    private final DialogueChatService dialogueChatService;

    @PostMapping(value = "/dialogue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatResponse (@RequestBody DialogueChatReqDto dialogueChatReqDto) {
        return dialogueChatService.streamDialogue(dialogueChatReqDto);
    }
}
