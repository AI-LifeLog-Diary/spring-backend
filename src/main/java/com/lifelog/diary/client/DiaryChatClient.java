package com.lifelog.diary.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DiaryChatClient {

    private final WebClient webClient;

    public Flux<String> streamDiary(String conversation, String accessToken) {
        Map<String, String> request = Map.of("conversation", conversation);

        return webClient.post()
                .uri("/chat/stream-diary")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(Objects::nonNull);
    }
}

