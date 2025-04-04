package com.lifelog.diary.service;

import com.lifelog.diary.dto.DialogueChatReqDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class DialogueChatService {

    private final WebClient webClient;

    public DialogueChatService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<String> streamDialogue(DialogueChatReqDto dialogueChatReqDto) {
        return webClient.post()
                .uri("/stream-dialogue")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(dialogueChatReqDto)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> data != null && !data.isBlank());
    }
}
