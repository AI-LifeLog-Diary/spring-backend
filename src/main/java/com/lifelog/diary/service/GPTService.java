package com.lifelog.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${OPEN_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private WebClient webClient;

    @PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public Flux<String> streamDiaryFromConversation(String conversation) {
        Map<String, Object> body = buildRequestBody(conversation, true);

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .flatMap(this::parseStreamChunk)
                .onErrorResume(e -> {
                    System.err.println("[GPTService] 스트리밍 오류: " + e.getMessage());
                    return Flux.empty();
                });
    }

    private Map<String, Object> buildRequestBody(String conversation, boolean stream) {
        return Map.of(
                "model", "gpt-4",
                "stream", stream,
                "messages", List.of(Map.of("role", "user", "content", createPrompt(conversation)))
        );
    }

    private Flux<String> parseStreamChunk(String chunk) {
        if (chunk == null || chunk.isBlank()) return Flux.empty();
        if ("[DONE]".equals(chunk.trim())) return Flux.empty();

        try {
            JsonNode root = objectMapper.readTree(chunk);
            JsonNode contentNode = root.path("choices").get(0).path("delta").path("content");

            if (contentNode != null && !contentNode.isNull()) {
                return Flux.just(contentNode.asText());
            }
        } catch (IOException e) {
            System.err.println("[GPTService] JSON 파싱 실패: " + e.getMessage());
        }
        return Flux.empty();
    }

    private String createPrompt(String conversation) {
        String template = """
                다음은 한 사용자가 오늘 하루에 대해 이야기한 대화 내용이야. 이 내용을 바탕으로 자연스럽고 일기처럼 정리해줘.
                
                대화 내용:
                {conversation}
                
                조건:
                - 날짜 없이 시작
                - 문어체, 일기체로 작성
                - 중복 없이 자연스럽게 연결
                - 3~5문장 정도로 작성
                
                작성된 일기:
                """;

        return template.replace("{diaryContent}", conversation);
    }
}