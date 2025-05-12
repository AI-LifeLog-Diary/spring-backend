package com.lifelog.diary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GPTService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String DIARY_PROMPT_TEMPLATE = """
        다음은 한 사용자가 오늘 하루에 대해 이야기한 대화 내용이야. 이 내용을 바탕으로 자연스럽고 일기처럼 정리해줘.

        대화 내용:
        %s

        조건:
        - 날짜 없이 시작
        - 문어체, 일기체로 작성
        - 중복 없이 자연스럽게 연결
        - 3~5문장 정도로 작성

        작성된 일기:
        """;

    public GPTService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateDiaryFromConversation(String conversation) {
        String prompt = String.format(DIARY_PROMPT_TEMPLATE, conversation.trim());

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 300
        );

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return ((String) message.get("content")).trim();
                })
                .block(); // 동기 처리
    }
}
