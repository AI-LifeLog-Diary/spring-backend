package com.lifelog.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelog.diary.dto.ChatMessageResDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowQuestionService {

    @Value("${OPEN_API_KEY}")
    private String apiKey;

    private WebClient webClient;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public Flux<ChatMessageResDto> streamFollowUpQuestion(String diaryContent) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "stream", true,
                "messages", List.of(Map.of("role", "user", "content", createPrompt(diaryContent)))
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM) // SSE 응답 수신
                .bodyValue(requestBody)
                .exchangeToFlux(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToFlux(String.class)
                                .flatMap(this::parseSSE)
                                .onErrorContinue((e, obj) -> e.printStackTrace());
                    } else {
                        return Flux.error(new RuntimeException("OpenAI API request failed: " + response.statusCode()));
                    }
                });
    }

    private String createPrompt(String diaryContent) {
        String template = """
                나는 앱 사용자가 오늘 한 일에 대해서 챗봇과 자연스럽게 대화를 나누고, 해당 대화를 기반으로 자동으로 일기를 생성해주는 기능을 개발하고 있어.
                이제 당신은 사용자가 오늘 한 일에 대해 질문하는 챗봇이야.
                사용자와의 전체 대화 맥락을 고려해서 자연스러운 질문을 한 문장으로, 반말로 만들어야 해.
                또한 사용자와의 대화 중 이미 네가 했던 질문은 다시 반복하지 않는 것이 중요해.
                
                예시 대화 1.
                사용자 : 안녕
                챗봇 : 안녕? 오늘은 무슨 일이 있었어?
                사용자 : 카페에서 공부했어.
                챗봇 : 오, 멋지다! 어떤 공부를 했어?
                사용자 : iOS 앱 만들기 강의를 들었어.
                챗봇 : 우와! 강의를 들으면서 어떤 앱을 만들고 있는데?
                사용자 : 당근마켓 로그인 페이지를 똑같이 만들어보고 있어.
                챗봇 : 좋네. 공부하면서 어떤 기분이 들었어?
                사용자 : 내 손으로 직접 앱을 만든다는 게 신기하고 재밌었어.
                챗봇 : 이야기를 들려주어서 고마워. 지금까지의 대화를 바탕으로 일기를 생성할게.
                
                예시 대화 2.
                사용자 : 안녕
                챗봇 : 안녕? 오늘은 무슨 일이 있었어?
                사용자 : 학교에 가서 졸업 프로젝트 발표를 했어.
                챗봇 : 오, 프로젝트는 어떤 내용이야?
                사용자 : 사용자가 일기를 작성하면 해당 일기 내용에 알맞은 AI 이미지를 생성해주는 서비스를 만들고 있어
                챗봇 : 멋진 프로젝트네! 오늘 발표에서 어려운 점은 없었어?
                사용자 : 발표 준비를 하나도 못해서 너무 긴장했어.
                챗봇 : 정말 긴장됐겠네. 어떻게 해결했어?
                사용자 : 해결 못하고 끝까지 긴장한 채로 발표했어. 너무 아쉬워.
                챗봇 : 다음 번에 더 좋은 발표를 하려면 어떻게 해야 할까?
                사용자 : 발표 하루 전에 대본을 짜봐야겠어.
                챗봇 : 이야기를 들려주어서 고마워. 지금까지의 대화를 바탕으로 일기를 생성할게.
                
                예시 대화 3.
                사용자 : 안녕
                챗봇 : 안녕? 오늘은 무슨 일이 있었어?
                사용자 : 집에 누워있었어.
                챗봇 : 그랬구나. 누워서 무엇을 했어?
                사용자 : 핸드폰으로 유튜브 봤어.
                챗봇 : 재밌는 영상이나 콘텐츠 있었어?
                사용자 : 좋아하는 당구 유튜브 채널이 있는데 재밌더라.
                챗봇 : 우와 재밌겠는걸? 오늘 집에 있으면서 어떤 기분을 느꼈어?
                사용자 : 오랜만에 푹 쉬는 것 같아서 좋았어.
                챗봇 : 이야기를 들려주어서 고마워. 지금까지의 대화를 바탕으로 일기를 생성할게.
                
                대화 스타일은 다음 조건을 따라야 해:
                - 구어체, 반말을 사용해. (예: "~했어?", "~였어?", "~했는데?")
                - 너무 형식적인 말투는 피하고 자연스럽게 말해줘.
                - 전체 대화를 고려해서 사용자가 말한 구체적인 활동에 대해 자연스럽게 더 궁금한 걸 묻거나,
                  이미 충분히 구체적이면 감정을 물어보고 대화를 마무리해야 해.
                - 질문은 한 문장으로 해줘.
                - "무엇을 무엇을 했니?" 같은 이상한 표현은 쓰지 마.
                
                다음은 지금까지의 대화야. 홀수번째 줄은 챗봇이, 짝수번째 줄은 사용자가 한 말임을 꼭 고려해야 해:
                \"\"\"
                {diaryContent}
                \"\"\"
                
                위 대화 흐름에 맞춰 자연스럽게 이어지면서도 지금까지 대화에서는 하지 않았던 질문을 한 문장으로 만들어줘.
                """;
        return template.replace("{diaryContent}", diaryContent);
    }

    private Flux<ChatMessageResDto> parseSSE(String line) {
        if (line.startsWith("data: ")) {
            String jsonPart = line.substring("data: ".length());

            if ("[DONE]".equals(jsonPart.trim())) {
                return Flux.empty();
            }

            try {
                JsonNode node = new ObjectMapper().readTree(jsonPart);
                JsonNode contentNode = node.path("choices").get(0).path("delta").path("content");
                if (!contentNode.isMissingNode()) {
                    return Flux.just(ChatMessageResDto.of(contentNode.asText(), null));
                }
            } catch (Exception e) {
                return Flux.error(e);
            }
        }
        return Flux.empty();
    }
}