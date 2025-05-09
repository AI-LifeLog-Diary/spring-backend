package com.lifelog.diary.client;

import com.lifelog.diary.dto.DialogueChatReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChatClient {

    private final WebClient webClient;

    public Flux<String> streamDialogue(DialogueChatReqDto dialogueChatReqDto) {
        return webClient.post()
                .uri("/stream-dialogue")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(dialogueChatReqDto)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(Objects::nonNull);
    }
}
