package com.lifelog.diary.client;

import com.lifelog.diary.security.util.SecurityUtil;
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

    public String getDiary(String conversation, String currentDiary) {
        Map<String, String> request = Map.of(
                "conversation", conversation,
                "currentDiary", currentDiary
        );

        return webClient.post()
                .uri("/chat/diary")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(Objects.requireNonNull(SecurityUtil.getCurrentAccessToken())))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

