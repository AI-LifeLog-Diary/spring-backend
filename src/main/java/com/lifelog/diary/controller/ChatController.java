package com.lifelog.diary.controller;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.ChatMessageReqDto;
import com.lifelog.diary.service.AccountService;
import com.lifelog.diary.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final AccountService accountService;

//    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> streamDiaryFromConversation(
//            @RequestBody ChatMessageReqDto reqDto) {
//        return Mono.fromCallable(accountService::getCurrentUser)
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMapMany(user -> chatService.chatAndCreateDiary(
//                        reqDto.getConversation(),
//                        reqDto.getCurrentDiary()
//                ));
//    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamDiaryFromConversation(@RequestBody ChatMessageReqDto reqDto) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMapMany(ctx -> Flux.defer(() -> {
                    SecurityContextHolder.setContext(ctx);
                    try {
                        User user = accountService.getCurrentUser();
                        return chatService.chatAndCreateDiary(
                                reqDto.getConversation(),
                                reqDto.getCurrentDiary(),
                                user
                        );
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}
