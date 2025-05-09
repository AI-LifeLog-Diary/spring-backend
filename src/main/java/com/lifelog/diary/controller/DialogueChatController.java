package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.dto.DialogueChatMessageResDto;
import com.lifelog.diary.dto.DialogueChatSessionResDto;
import com.lifelog.diary.dto.DialogueChatReqDto;
import com.lifelog.diary.service.DialogueChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/dialogue")
public class DialogueChatController {

    private final DialogueChatService dialogueChatService;


    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatResponse(@RequestBody DialogueChatReqDto dialogueChatReqDto) throws Exception {
        return dialogueChatService.streamDialogue(dialogueChatReqDto);
    }

    @GetMapping("/{userId}/chat-list")
    public ResponseDto<DialogueChatSessionResDto> getChatList(@PathVariable("userId") Long userId) {
        List<DialogueChatSessionResDto> data = dialogueChatService.getChatList(userId);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "채팅 목록 조회에 성공했습니다.");
        return new ResponseDto<>(meta, data);
    }

    @GetMapping("/{userId}/chat-list/{sessionId}")
    public ResponseDto<DialogueChatMessageResDto> getChatDetail(@PathVariable("userId") Long userId,
                                                                @PathVariable("sessionId") Long sessionId,
                                                                @RequestParam(required = false) Long cursor,
                                                                @RequestParam(defaultValue = "5") int size) {
        DialogueChatMessageResDto data = dialogueChatService.getChatDetail(userId, sessionId, cursor, size);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "채팅 상세 조회에 성공했습니다.");
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }
}
