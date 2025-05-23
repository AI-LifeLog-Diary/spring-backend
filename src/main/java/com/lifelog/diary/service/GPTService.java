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
        - 대화에 없는 내용을 지어내지 않을 것
        - 대화에 나온 내용만 정확하게 바탕으로 작성할 것
        - 대화 내용 외의 정보는 절대로 포함하지 말 것
        - 내용의 추가나 변형 없이 대화 내용을 그대로 바탕으로 작성
        - 대화에서 사용된 표현과 일치하는 형태로 작성하되, 일기 형식에 맞게 자연스럽게 연결

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
                "temperature", 0.2,
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
