package com.lifelog.diary.controller;

import com.lifelog.diary.dto.ChatMessageReqDto;
import com.lifelog.diary.service.AccountService;
import com.lifelog.diary.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final AccountService accountService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamDiaryFromConversation(
            @RequestBody ChatMessageReqDto reqDto) {
        return Mono.fromCallable(accountService::getCurrentUser)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(user -> chatService.chatAndCreateDiary(
                        reqDto.getConversation(),
                        reqDto.getCurrentDiary()
                ));
    }
}
