package com.lifelog.diary.controller;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.ChatMessageReqDto;
import com.lifelog.diary.dto.ChatMessageResDto;
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

    @PostMapping("/stream")
    public ChatMessageResDto streamDiaryFromConversation(@RequestBody ChatMessageReqDto reqDto) {
        User user = accountService.getCurrentUser(); // 정상 작동 확인된 인증 방식 사용
        return chatService.chatAndCreateDiary(
                reqDto.getConversation(),
                reqDto.getCurrentDiary(),
                user
        );
    }
}
