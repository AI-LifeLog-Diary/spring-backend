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
    private final DiaryService diaryService;
    private final FollowQuestionService followQuestionService;

    public Flux<String> chatAndCreateDiary(String conversation, String currentDiary, User user) {
        boolean isDiaryPresent = currentDiary != null && !currentDiary.isBlank();

        if (isDiaryPresent) {
            StringBuilder diaryBuilder = new StringBuilder();
            return gptService.streamDiaryFromConversation(conversation)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(diaryBuilder::append)
                    .doOnComplete(() -> diaryService.createDiary(user, new DiaryReqDto(diaryBuilder.toString())));
        }

        if (conversation == null || conversation.isBlank()) {
            return Flux.just("data: " + "안녕, 오늘은 어떤 일이 있었어?");
        }

        Flux<String> followUpFlux = followQuestionService.streamFollowUpQuestionFromConversation(conversation)
                .map(token -> "FOLLOWUP: " + token);

        Flux<String> diaryFlux = gptService.streamDiaryFromConversation(conversation)
                .map(token -> "DIARY: " + token);

        return Flux.concat(followUpFlux, diaryFlux);
    }
}