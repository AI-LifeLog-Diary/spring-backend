package com.lifelog.diary.service;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.DiaryReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GPTService gptService;
    private final FollowQuestionService followQuestionService;

    public Flux<String> chatAndCreateDiary(String conversation, String currentDiary, String accessToken) {
        boolean isDiaryPresent = currentDiary != null && !currentDiary.isBlank();

        if (isDiaryPresent) {
            StringBuilder diaryBuilder = new StringBuilder();
            return gptService.streamDiaryFromConversation(conversation, accessToken)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(diaryBuilder::append)
                    .map(token -> "DIARY: " + token);
        }

        if (conversation == null || conversation.isBlank()) {
            return Flux.just("data: " + "안녕, 오늘은 어떤 일이 있었어?");
        }

        Flux<String> followUpFlux = followQuestionService.streamFollowUpQuestionFromConversation(conversation, accessToken)
                .map(token -> "FOLLOWUP: " + token);

        Flux<String> diaryFlux = gptService.streamDiaryFromConversation(conversation, accessToken)
                .map(token -> "DIARY: " + token);

        return Flux.concat(followUpFlux, diaryFlux);
    }
}