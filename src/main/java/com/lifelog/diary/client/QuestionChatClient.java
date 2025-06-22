package com.lifelog.diary.client;

import com.lifelog.diary.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QuestionChatClient {

    private final WebClient webClient;

    public String getFollowUpQuestion(String conversation) {
        Map<String, String> request = Map.of("conversation", conversation);

        return webClient.post()
                .uri("/chat/follow-up-question")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(Objects.requireNonNull(SecurityUtil.getCurrentAccessToken())))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

