package com.lifelog.diary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class FollowQuestionService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public FollowQuestionService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateFollowUpQuestion(String conversation) {
        String prompt = buildPrompt(conversation);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 사용자의 오늘 하루를 잘 들어주는 친근한 챗봇이야. 자연스럽고 따뜻한 말투로 대화를 이어가. 이미 했던 질문은 반복하지 말아줘."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4,
                "max_tokens", 100
        );

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
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

    private String buildPrompt(String conversation) {
        return "나는 앱 사용자가 오늘 한 일에 대해서 챗봇과 자연스럽게 대화를 나누고, 해당 대화를 기반으로 자동으로 일기를 생성해주는 기능을 개발하고 있어." +
                " 이제 당신은 사용자가 오늘 한 일에 대해 질문하는 챗봇이야." +
                " 사용자와의 전체 대화 맥락을 고려해서 자연스러운 질문을 한 문장으로, 반말로 만들어야 해." +
                " 또한 사용자와의 대화 중 이미 네가 했던 질문은 다시 반복하지 않는 것이 중요해." +
                " 대화 스타일은 구어체, 반말을 사용해. (예: \"~했어?\", \"~였어?\", \"~했는데?\")" +
                " \"\"\"\n" +
                conversation + "\n\"\"\"\n" +
                "위 대화 흐름에 맞춰 자연스럽게 이어지면서도 지금까지 대화에서는 하지 않았던 질문을 한 문장으로 만들어줘.";
    }
}
